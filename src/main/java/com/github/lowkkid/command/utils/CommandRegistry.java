package com.github.lowkkid.command.utils;

import com.github.lowkkid.command.Command;
import com.github.lowkkid.command.ExternalCommand;
import com.github.lowkkid.utils.FileUtils;
import org.reflections.Reflections;

import java.util.*;

public class CommandRegistry {

    private static final String COMMANDS_PACKAGE = "com.github.lowkkid.command";

    public static CommandRegistry getInstance() {
        return CommandRegistryHolder.INSTANCE;
    }

    private final Map<String, Command> executableCommands;
    private final Set<String> builtInCommands;

    {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");
    }

    public Optional<Command> getExecutableCommand(String name) {
        return Optional.ofNullable(executableCommands.get(name))
                .or(() -> {
                    var externalPath = FileUtils.existsInPathDirectories(name);
                    if (externalPath != null) {
                        var externalExecutableCommand = new ExternalCommand(name);
                        putExecutableCommand(name, externalExecutableCommand);
                        return Optional.of(externalExecutableCommand);

                    }
                    return Optional.empty();
                });
    }

    private void putExecutableCommand(String name, Command command) {
        if (executableCommands.containsKey(name)) {
            throw new IllegalArgumentException(String.format("Command '%s' already exists", name));
        }
        executableCommands.put(name, command);
    }

    public boolean isBultInCommand(String command) {
        return builtInCommands.contains(command);
    }


    private CommandRegistry() {
        this.executableCommands = new HashMap<>();
        this.builtInCommands = new HashSet<>();
        registerBuiltInCommands();
    }



    private void registerBuiltInCommands() {
        Reflections reflections = new Reflections(COMMANDS_PACKAGE);
        Set<Class<? extends Command>> commandClasses =
                reflections.getSubTypesOf(Command.class);

        for (Class<? extends Command> clazz : commandClasses) {
            var builtInCommandAnnotation = clazz.getAnnotation(BuiltInCommand.class);
            if (builtInCommandAnnotation != null) {
                try {
                    Command command = clazz.getDeclaredConstructor().newInstance();
                    putExecutableCommand(builtInCommandAnnotation.name(), command);
                    builtInCommands.add(builtInCommandAnnotation.name());
                } catch (Exception e) {
                    System.err.println("Failed to instantiate: " + clazz.getName());
                }
            }
        }
    }

    private static class CommandRegistryHolder {
        private static final CommandRegistry INSTANCE = new CommandRegistry();
    }
}
