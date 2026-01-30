package com.github.lowkkid.jsh.command;

import java.util.List;

public class Exit extends Command {

    @Override
    public void executeWithException(List<String> args) {

    }

    @Override
    public boolean shouldBreak() {
        return true;
    }
}
