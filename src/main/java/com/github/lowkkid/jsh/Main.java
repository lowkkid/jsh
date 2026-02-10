package com.github.lowkkid.jsh;

import static com.github.lowkkid.jsh.command.utils.HistoryUtils.configureHistory;
import static com.github.lowkkid.jsh.config.EnvConfigReader.HOME;

import com.github.lowkkid.jsh.command.utils.CommandRegistry;
import com.github.lowkkid.jsh.command.utils.HistoryUtils;
import com.github.lowkkid.jsh.executor.SegmentedExecutor;
import com.github.lowkkid.jsh.parser.InputParser;
import com.github.lowkkid.jsh.ui.CommandHighlighter;
import com.github.lowkkid.jsh.ui.PromptBuilder;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jline.builtins.Completers.FilesCompleter;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class Main {

    private static final InputParser PARSER = InputParser.getInstance();
    private static final CommandRegistry COMMANDS_REGISTRY = CommandRegistry.getInstance();
    private static final PromptBuilder PROMPT_BUILDER = new PromptBuilder();
    private static final SegmentedExecutor PIPELINE_EXECUTOR =
            new SegmentedExecutor(COMMANDS_REGISTRY);

    public static Path currentDir = Paths.get(HOME);
    public static Terminal terminal;


    static void main() throws Exception {
        var terminal = TerminalBuilder
                .builder()
                .system(true)
                .build();
        Main.terminal = terminal;

        var commandCompleter = new ArgumentCompleter(
                new StringsCompleter(COMMANDS_REGISTRY.getAllCommands()),
                new FilesCompleter(() -> currentDir)
        );
        var readerBuilder = LineReaderBuilder.builder()
                .terminal(terminal)
                .highlighter(new CommandHighlighter(COMMANDS_REGISTRY))
                .completer(commandCompleter);

        configureHistory(readerBuilder);

        var reader = readerBuilder.build();

        HistoryUtils.afterInitialization();


        while (true) {
            try {
                String prompt = PROMPT_BUILDER.build(currentDir);
                String userInput = reader.readLine(prompt);

                var commandsAndArgs = PARSER.getCommandAndArgs(userInput);

                SegmentedExecutor.ExecutionResult result;
                if (commandsAndArgs.size() == 1) {
                    result = PIPELINE_EXECUTOR.executeSingle(commandsAndArgs.getFirst());
                } else {
                    result = PIPELINE_EXECUTOR.executePipeline(commandsAndArgs);
                }

                if (result.shouldBreak()) {
                    break;
                }

            } catch (UserInterruptException e) {
                // Ctrl+C
            }
        }
        HistoryUtils.HISTORY.save();
        terminal.close();

    }

}
