package com.github.lowkkid;

import com.github.lowkkid.command.Command;
import com.github.lowkkid.command.Echo;
import com.github.lowkkid.command.Exit;
import com.github.lowkkid.command.Type;
import com.github.lowkkid.exception.CommandNotFoundException;
import com.github.lowkkid.utils.RunUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Main {

    private static final Scanner SCANNER = new Scanner(System.in);
    public static final String PATH = System.getenv("PATH");
    private static final Set<String> BUILT_IN_COMMANDS = Set.of("echo", "exit", "type");
    private static final Map<String, Command> BUILT_IN_COMMANDS_MAP = Map.of(
            "echo", new Echo(),
            "exit", new Exit(),
            "type", new Type()
    );

    public static void main(String[] args) throws Exception {

        while (true) {
            System.out.print("$ ");

            var userInput = SCANNER.nextLine();
            var commandAndArgs = getCommandAndArgs(userInput);
            var command = commandAndArgs.command;
            var arguments = commandAndArgs.arguments;

            var executableCommand = BUILT_IN_COMMANDS_MAP.get(command);

            if (executableCommand == null) {
                try {
                    RunUtils.runExternal(command, arguments);
                } catch (CommandNotFoundException e) {
                    System.out.println(e.getMessage());
                }
                continue;
            }

            executableCommand.execute(arguments);

            if (executableCommand.shouldBreak()) {
                break;
            }
        }
    }

    public static boolean isBultInCommand(String command) {
        return BUILT_IN_COMMANDS_MAP.containsKey(command);
    }

    private static CommandAndArgs getCommandAndArgs(String userInput) {
        String[] commandAndArgs = userInput.split(" ");
        return new CommandAndArgs(commandAndArgs[0], Arrays.copyOfRange(commandAndArgs, 1, commandAndArgs.length));
    }

    private static record CommandAndArgs(String command, String[] arguments) {
    }

}
