package com.github.lowkkid;

import com.github.lowkkid.command.utils.CommandRegistry;
import com.github.lowkkid.parser.InputParser;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    private static final InputParser PARSER = InputParser.getInstance();
    private static final CommandRegistry COMMANDS_REGISTRY = CommandRegistry.getInstance();

    public static Path currentDir = Paths.get("").toAbsolutePath();


    public static void main(String[] args) throws Exception {
        Terminal terminal = TerminalBuilder.builder().system(true).build();
        // Complete with fixed strings
        Completer stringsCompleter = new StringsCompleter(COMMANDS_REGISTRY.getAllCommands());
        LineReader reader = LineReaderBuilder.builder().terminal(terminal).option(LineReader.Option.DISABLE_EVENT_EXPANSION, true).completer(stringsCompleter).build();


        while (true) {
            try {
                String userInput = reader.readLine("$ ");

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
        terminal.close();

    }

}
