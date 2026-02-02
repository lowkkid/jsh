package com.github.lowkkid.jsh.command;

import com.github.lowkkid.jsh.command.utils.CommandRegistry;
import com.github.lowkkid.jsh.utils.FileUtils;
import java.util.List;
import java.util.function.Function;

public class Type extends Command {

    private final CommandRegistry registry;
    private final Function<String, String> pathLookup;

    public Type(CommandRegistry registry) {
        this(registry, FileUtils::existsInPathDirectories);
    }

    public Type(CommandRegistry registry, Function<String, String> pathLookup) {
        this.registry = registry;
        this.pathLookup = pathLookup;
    }

    @Override
    public void executeWithException(List<String> args) {
        args.forEach((arg) -> {
            if (registry.isBultInCommand(arg)) {
                stdOut.println(arg + " is a shell builtin");
            } else {
                String cmdDir = pathLookup.apply(arg);
                if (cmdDir != null) {
                    stdOut.println(arg + " is " + cmdDir);
                } else {
                    stdOut.println(arg + ": not found");
                }
            }
        });
    }
}
