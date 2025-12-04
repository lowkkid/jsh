package com.github.lowkkid.command;

public class Exit implements Command {

    @Override
    public void execute(String[] args) {

    }

    @Override
    public boolean shouldBreak() {
        return true;
    }
}
