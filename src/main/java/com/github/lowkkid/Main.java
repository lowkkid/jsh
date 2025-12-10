package com.github.lowkkid;

import com.github.lowkkid.command.utils.CommandRegistry;
import com.github.lowkkid.parser.InputParser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final InputParser PARSER = InputParser.getInstance();
    private static final CommandRegistry COMMANDS_REGISTRY = CommandRegistry.getInstance();

    public static Path currentDir = Paths.get("").toAbsolutePath();

    static void main()  {

        while (true) {
            System.out.print("$ ");

            var userInput = SCANNER.nextLine().trim();
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
                }  else {
                    executableCommand.execute(arguments);
                }
                if (executableCommand.shouldBreak()) {
                    break;
                }
            }
        }
    }

}
