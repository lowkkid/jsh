package com.github.lowkkid.jsh;

import static com.github.lowkkid.jsh.command.utils.HistoryUtils.configureHistory;
import static com.github.lowkkid.jsh.config.EnvConfigReader.HOME;

import com.github.lowkkid.jsh.command.utils.CommandRegistry;
import com.github.lowkkid.jsh.command.utils.HistoryUtils;
import com.github.lowkkid.jsh.parser.InputParser;
import com.github.lowkkid.jsh.ui.PromptBuilder;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.TerminalBuilder;

public class Main {

    private static final InputParser PARSER = InputParser.getInstance();
    private static final CommandRegistry COMMANDS_REGISTRY = CommandRegistry.getInstance();
    private static final PromptBuilder PROMPT_BUILDER = new PromptBuilder();

    public static Path currentDir = Paths.get(HOME);


     static void main() throws Exception {
        var terminal = TerminalBuilder.
                builder()
                .system(true)
                .build();

        var stringsCompleter = new StringsCompleter(COMMANDS_REGISTRY.getAllCommands());
        var readerBuilder = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(stringsCompleter);

        configureHistory(readerBuilder);

        var reader = readerBuilder.build();

         HistoryUtils.afterInitialization();


        while (true) {
            try {
                String prompt = PROMPT_BUILDER.build(currentDir);
                String userInput = reader.readLine(prompt);

                var commandAndArgs = PARSER.getCommandAndArgs(userInput);
                var command = commandAndArgs.getCommand();
                var arguments = commandAndArgs.getArguments();
                var shouldBeRedirected = commandAndArgs.shouldBeRedirected();

                var executableCommandOpt = COMMANDS_REGISTRY.getExecutableCommand(command);

                if (executableCommandOpt.isEmpty()) {
                    System.out.println(command + ": not found");
                } else {
                    var executableCommand = executableCommandOpt.get();
                    if (shouldBeRedirected) {
                        executableCommand.executeWithRedirect(
                                arguments, commandAndArgs.getRedirectOptions());
                    } else {
                        executableCommand.execute(arguments);
                    }
                    if (executableCommand.shouldBreak()) {
                        break;
                    }
                }
            } catch (UserInterruptException e) {
                // Ctrl+C
            }
        }
        HistoryUtils.HISTORY.save();
        terminal.close();

    }

}
