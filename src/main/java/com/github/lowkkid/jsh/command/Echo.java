package com.github.lowkkid.jsh.command;

import java.util.List;

public class Echo extends Command {

    @Override
    public void executeWithException(List<String> args) {
        args.forEach(arg -> stdOut.print(arg + " "));
        stdOut.println();
    }
}
