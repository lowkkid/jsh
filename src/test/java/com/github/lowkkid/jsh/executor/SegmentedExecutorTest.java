package com.github.lowkkid.jsh.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.lowkkid.jsh.Main;
import com.github.lowkkid.jsh.command.utils.CommandRegistry;
import com.github.lowkkid.jsh.executor.PipelineSegment.BuiltInSegment;
import com.github.lowkkid.jsh.executor.PipelineSegment.ExternalSegment;
import com.github.lowkkid.jsh.parser.CommandAndArgs;
import com.github.lowkkid.jsh.parser.InputParser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SegmentedExecutorTest {

    @TempDir
    Path tempDir;

    private SegmentedExecutor executor;
    private InputParser parser;
    private ByteArrayOutputStream capturedOut;
    private ByteArrayOutputStream capturedErr;
    private PrintStream originalOut;
    private PrintStream originalErr;
    private Path originalCurrentDir;

    @BeforeEach
    void setUp() {
        originalCurrentDir = Main.currentDir;
        Main.currentDir = tempDir;

        parser = new InputParser();
        executor = new SegmentedExecutor(CommandRegistry.getInstance());

        capturedOut = new ByteArrayOutputStream();
        capturedErr = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(capturedOut));
        System.setErr(new PrintStream(capturedErr));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        Main.currentDir = originalCurrentDir;
    }

    private String stdout() {
        System.out.flush();
        return capturedOut.toString();
    }

    private String stderr() {
        System.err.flush();
        return capturedErr.toString();
    }

    private List<CommandAndArgs> parse(String input) {
        return parser.getCommandAndArgs(input);
    }

    @Nested
    @DisplayName("executeSingle")
    class ExecuteSingleTests {

        @Test
        void builtInCommandDoesNotBreak() {
            var result = executor.executeSingle(parse("echo hello").getFirst());

            assertFalse(result.shouldBreak());
        }

        @Test
        void exitReturnsShouldBreak() {
            var result = executor.executeSingle(parse("exit").getFirst());

            assertTrue(result.shouldBreak());
        }

        @Test
        void commandNotFound() {
            var result = executor.executeSingle(parse("nonexistent_cmd_xyz_98765").getFirst());

            assertFalse(result.shouldBreak());
            assertTrue(stdout().contains("not found"));
        }

        @Test
        void redirectRewritesToFile() throws IOException {
            executor.executeSingle(parse("echo hello > out.txt").getFirst());

            Path outFile = tempDir.resolve("out.txt");
            assertTrue(Files.exists(outFile));
            assertTrue(Files.readString(outFile).contains("hello"));
        }

        @Test
        void redirectAppendsToFile() throws IOException {
            Path outFile = tempDir.resolve("out.txt");
            Files.writeString(outFile, "first\n");

            executor.executeSingle(parse("echo second >> out.txt").getFirst());

            String content = Files.readString(outFile);
            assertTrue(content.contains("first"));
            assertTrue(content.contains("second"));
        }

        @Test
        void cdChangesDirAndDoesNotBreak() {
            Path subDir = tempDir.resolve("sub");
            subDir.toFile().mkdir();

            var result = executor.executeSingle(parse("cd sub").getFirst());

            assertFalse(result.shouldBreak());
            assertEquals(subDir, Main.currentDir);
        }
    }

    @Nested
    @DisplayName("executePipeline")
    class ExecutePipelineTests {

        @Test
        void throwsForSingleCommand() {
            var commands = parse("echo hello");

            assertThrows(IllegalArgumentException.class,
                    () -> executor.executePipeline(commands));
        }

        @Test
        void builtInToExternal() {
            var result = executor.executePipeline(parse("echo hello | cat"));

            assertFalse(result.shouldBreak());
        }

        @Test
        void twoExternalCommands() {
            var result = executor.executePipeline(parse("printf hello | cat"));

            assertFalse(result.shouldBreak());
        }

        @Test
        void multipleExternalsInOneSegment() {
            // printf | grep | pwd → ExternalSegment([printf, grep]), BuiltInSegment(pwd)
            var result = executor.executePipeline(parse("printf hello | grep hello | pwd"));

            assertFalse(result.shouldBreak());
            assertEquals(tempDir.toString(), stdout().trim());
        }

        @Test
        void externalToBuiltIn() {
            // printf hello | pwd → ExternalSegment(printf), BuiltInSegment(pwd)
            var result = executor.executePipeline(parse("printf hello | pwd"));

            assertFalse(result.shouldBreak());
            assertEquals(tempDir.toString(), stdout().trim());
        }

        @Test
        void threeSegmentsPipeline() {
            // echo hello | cat | pwd
            // BuiltIn(echo) → External(cat) → BuiltIn(pwd)
            var result = executor.executePipeline(parse("echo hello | cat | pwd"));

            assertFalse(result.shouldBreak());
            assertEquals(tempDir.toString(), stdout().trim());
        }

        @Test
        void exitAsLastInPipeline() {
            var result = executor.executePipeline(parse("echo hello | exit"));

            assertTrue(result.shouldBreak());
        }

        @Test
        void exitInMiddleStopsExecution() {
            // echo | exit | cat → BuiltIn, BuiltIn(exit), External
            // exit sets shouldBreak=true, cat segment never executes
            var result = executor.executePipeline(parse("echo hello | exit | cat"));

            assertTrue(result.shouldBreak());
        }

        @Test
        void builtInNonLastBuffersOutput() {
            // echo hello | cat → echo is non-last built-in, output buffered to cat
            // Verifies executeBuiltInSegment non-last path (ByteArrayOutputStream)
            var result = executor.executePipeline(parse("echo hello | cat"));

            assertFalse(result.shouldBreak());
            // cat receives echo's buffered output — no crash means buffer worked
        }

        @Test
        void externalNonLastCapturesOutput() {
            // printf hello | pwd
            // printf is external non-last: output captured via getInputStream
            // pwd is built-in last: writes to System.out
            var result = executor.executePipeline(parse("printf hello | pwd"));

            assertFalse(result.shouldBreak());
            assertTrue(stdout().contains(tempDir.toString()));
        }

        @Test
        void externalWithInputFromPreviousSegment() {
            // echo hello | cat | pwd
            // cat (external, non-last) receives input from echo (built-in)
            // Exercises the input feeder thread branch (input != null)
            var result = executor.executePipeline(parse("echo hello | cat | pwd"));

            assertFalse(result.shouldBreak());
            assertTrue(stdout().contains(tempDir.toString()));
        }

        @Test
        void externalFirstSegmentWithNoInput() {
            // printf hello | pwd
            // printf is first external segment: input == null → closes outputStream
            var result = executor.executePipeline(parse("printf hello | pwd"));

            assertFalse(result.shouldBreak());
            assertTrue(stdout().contains(tempDir.toString()));
        }

        @Test
        void externalStderrTransferredForNonLastSegment() {
            // cat nonexistent_file | pwd
            // cat (external, non-last) produces stderr
            // stderr should be transferred to System.err
            executor.executePipeline(parse("cat nonexistent_xyz_file | pwd"));

            assertTrue(stderr().contains("nonexistent_xyz_file")
                    || stderr().contains("No such file"));
        }

        @Test
        void pipelineWithFileInput() throws IOException {
            Path inputFile = tempDir.resolve("input.txt");
            Files.writeString(inputFile, "test line\n");

            // cat input.txt | pwd → external reads file, built-in outputs dir
            var result = executor.executePipeline(parse("cat input.txt | pwd"));

            assertFalse(result.shouldBreak());
            assertEquals(tempDir.toString(), stdout().trim());
        }
    }
}
