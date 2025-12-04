package com.github.lowkkid.command;

@FunctionalInterface
public interface Command {
    void execute(String[] args);

    default boolean shouldBreak() {
        return false;
    }
}
