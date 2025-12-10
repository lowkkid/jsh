package com.github.lowkkid.command;

import com.github.lowkkid.command.utils.BuiltInCommand;

import java.util.List;

@BuiltInCommand(name = "exit")
public class Exit extends Command {

    @Override
    public void executeWithException(List<String> args) {

    }

    @Override
    public boolean shouldBreak() {
        return true;
    }
}
