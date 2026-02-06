package com.github.lowkkid.jsh.executor;

import com.github.lowkkid.jsh.Main;
import com.github.lowkkid.jsh.command.Command;
import com.github.lowkkid.jsh.command.utils.CommandRegistry;
import com.github.lowkkid.jsh.executor.PipelineSegment.BuiltInSegment;
import com.github.lowkkid.jsh.executor.PipelineSegment.ExternalSegment;
import com.github.lowkkid.jsh.parser.CommandAndArgs;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Executes command pipelines using segment-based approach.
 *
 * <p>Pipelines are split into segments at built-in command boundaries.
 * External command segments use {@link ProcessBuilder#startPipeline(List)}
 * for efficient OS-level piping. Built-in commands are executed in the JVM
 * with buffered I/O between segments.
 *
 * <h2>Example</h2>
 * <pre>
 * Pipeline: cat file | grep foo | pwd | wc -l
 *
 * Segments:
 *   1. External [cat file, grep foo] → executed via startPipeline()
 *   2. BuiltIn  [pwd]                → executed in JVM
 *   3. External [wc -l]              → executed via startPipeline()
 *
 * Data flow:
 *   startPipeline([cat, grep]) → buffer → pwd → buffer → startPipeline([wc])
 * </pre>
 */
public class SegmentedExecutor implements CommandExecutor {

    private final CommandRegistry registry;

    public SegmentedExecutor(CommandRegistry registry) {
        this.registry = registry;
    }

    @Override
    public ExecutionResult executeSingle(CommandAndArgs commandAndArgs) {
        var command = commandAndArgs.command();
        var arguments = commandAndArgs.arguments();
        var shouldBeRedirected = commandAndArgs.shouldBeRedirected();

        var executableCommandOpt = registry.getExecutableCommand(command);

        if (executableCommandOpt.isEmpty()) {
            System.out.println(command + ": not found");
            return new ExecutionResult(false);
        }

        var executableCommand = executableCommandOpt.get();
        if (shouldBeRedirected) {
            executableCommand.executeWithRedirect(arguments, commandAndArgs.redirectOptions());
        } else {
            executableCommand.execute(arguments);
        }

        return new ExecutionResult(executableCommand.shouldBreak());
    }

    @Override
    public ExecutionResult executePipeline(List<CommandAndArgs> commandsAndArgs) {
        if (commandsAndArgs.size() < 2) {
            throw new IllegalArgumentException("Pipeline requires at least 2 commands");
        }

        List<PipelineSegment> segments = splitIntoSegments(commandsAndArgs);
        return executeSegments(segments);
    }

    /**
     * Splits a pipeline into segments at built-in command boundaries.
     *
     * <p>Consecutive external commands are grouped into a single segment.
     * Each built-in command becomes its own segment.
     */
    protected List<PipelineSegment> splitIntoSegments(List<CommandAndArgs> commandsAndArgs) {
        List<PipelineSegment> segments = new ArrayList<>();
        List<CommandAndArgs> currentExternalBatch = new ArrayList<>();

        for (CommandAndArgs cmdArgs : commandsAndArgs) {
            String commandName = cmdArgs.command();

            if (registry.isBultInCommand(commandName)) {
                if (!currentExternalBatch.isEmpty()) {
                    segments.add(new ExternalSegment(new ArrayList<>(currentExternalBatch)));
                    currentExternalBatch.clear();
                }

                var command = registry.getExecutableCommand(commandName);
                command.ifPresent(value -> segments.add(new BuiltInSegment(value, cmdArgs.arguments())));
            } else {
                currentExternalBatch.add(cmdArgs);
            }
        }

        if (!currentExternalBatch.isEmpty()) {
            segments.add(new ExternalSegment(new ArrayList<>(currentExternalBatch)));
        }

        return segments;
    }

    /**
     * Executes segments sequentially, passing buffered data between them.
     */
    private ExecutionResult executeSegments(List<PipelineSegment> segments) {
        byte[] buffer = null;
        boolean shouldBreak = false;

        for (int i = 0; i < segments.size(); i++) {
            PipelineSegment segment = segments.get(i);
            boolean isLast = (i == segments.size() - 1);

            InputStream input = (buffer != null)
                    ? new ByteArrayInputStream(buffer)
                    : null;

            SegmentResult result = switch (segment) {
                case BuiltInSegment builtIn -> executeBuiltInSegment(builtIn, input, isLast);
                case ExternalSegment external -> executeExternalSegment(external, input, isLast);
            };

            buffer = result.output();
            if (result.shouldBreak()) {
                shouldBreak = true;
                break;
            }
        }

        return new ExecutionResult(shouldBreak);
    }

    private record SegmentResult(byte[] output, boolean shouldBreak) {}

    /**
     * Executes a built-in command segment.
     */
    private SegmentResult executeBuiltInSegment(BuiltInSegment segment, InputStream input,
                                                boolean isLast) {
        Command command = segment.command();
        ByteArrayOutputStream output = isLast ? null : new ByteArrayOutputStream();

        if (isLast) {
            // last segment: write directly to System.out
            command.executeInPipeline(input, System.out, segment.args());
        } else {
            command.executeInPipeline(input, output, segment.args());
        }

        return new SegmentResult(
                output != null ? output.toByteArray() : null,
                command.shouldBreak()
        );
    }

    /**
     * Executes an external command segment using ProcessBuilder.startPipeline().
     */
    private SegmentResult executeExternalSegment(ExternalSegment segment, InputStream input,
                                                 boolean isLast) {
        List<ProcessBuilder> builders = new ArrayList<>(segment.commands().stream()
                .map(this::createProcessBuilder)
                .toList());

        // for last segment, redirect output directly to terminal (no buffering)
        if (isLast) {
            builders.getLast().redirectOutput(ProcessBuilder.Redirect.INHERIT);
            builders.getLast().redirectError(ProcessBuilder.Redirect.INHERIT);
        }

        try {
            List<Process> processes = ProcessBuilder.startPipeline(builders);
            Process first = processes.getFirst();
            Process last = processes.getLast();

            // Feed input from previous segment in a separate thread to avoid deadlock
            Thread inputFeeder = null;
            if (input != null) {
                final InputStream finalInput = input;
                inputFeeder = Thread.startVirtualThread(() -> {
                    try (var os = first.getOutputStream()) {
                        finalInput.transferTo(os);
                    } catch (IOException ignored) {
                        // Broken pipe is expected when downstream closes early
                    }
                });
            } else {
                first.getOutputStream().close();
            }

            byte[] output = null;
            if (!isLast) {
                // Capture output for next segment
                output = last.getInputStream().readAllBytes();
            }

            // Wait for the last process (others will terminate via SIGPIPE)
            last.waitFor();

            if (inputFeeder != null) {
                inputFeeder.join(100);
                if (inputFeeder.isAlive()) {
                    inputFeeder.interrupt();
                }
            }

            // Handle stderr for non-last segments
            if (!isLast) {
                for (Process p : processes) {
                    p.getErrorStream().transferTo(System.err);
                }
            }

            return new SegmentResult(output, false);

        } catch (IOException | InterruptedException e) {
            System.err.println("Pipeline error: " + e.getMessage());
            return new SegmentResult(null, false);
        }
    }

    private ProcessBuilder createProcessBuilder(CommandAndArgs cmdArgs) {
        var command = cmdArgs.command();
        var arguments = cmdArgs.arguments();

        return new ProcessBuilder(
                Stream.concat(Stream.of(command), arguments.stream()).toList())
                .directory(Main.currentDir.toFile());
    }
}
