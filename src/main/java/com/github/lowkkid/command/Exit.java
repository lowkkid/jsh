package com.github.lowkkid.command;

import java.util.List;

public class Exit implements Command {

    @Override
    public void execute(List<String> args) {

    }

    @Override
    public boolean shouldBreak() {
        return true;
    }
}
