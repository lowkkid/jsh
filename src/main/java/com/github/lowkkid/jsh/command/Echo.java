package com.github.lowkkid.jsh.command;

import java.util.List;

public class Echo extends Command {

    @Override
    public void executeWithException(List<String> args) {
        stdOut.println(String.join(" ", args));
    }
}
