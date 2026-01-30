package com.github.lowkkid.jsh.command;

import com.github.lowkkid.jsh.command.utils.BuiltInCommand;
import com.github.lowkkid.jsh.command.utils.CommandRegistry;
import com.github.lowkkid.jsh.utils.FileUtils;

import java.util.List;

@BuiltInCommand(name = "type")
public class Type extends Command {

    private CommandRegistry registry;

    @Override
    public void executeWithException(List<String> args) {
        args.forEach((arg) -> {
            if (registry().isBultInCommand(arg)) {
                stdOut.println(arg + " is a shell builtin");
            } else {
                String cmdDir = FileUtils.existsInPathDirectories(arg);
                if (cmdDir != null) {
                    stdOut.println(arg + " is " + cmdDir);
                } else {
                    stdOut.println(arg + ": not found");
                }
            }
        });
    }

    private CommandRegistry registry() {
        if (registry == null) {
            registry = CommandRegistry.getInstance();
        }
        return registry;
    }
}
