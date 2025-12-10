package com.github.lowkkid.command;

import com.github.lowkkid.command.utils.BuiltInCommand;

import java.util.List;

@BuiltInCommand(name = "echo")
public class Echo extends Command {

    @Override
    public void executeWithException(List<String> args) {
        args.forEach(arg -> stdOut.print(arg + " "));
        stdOut.println();
    }
}
