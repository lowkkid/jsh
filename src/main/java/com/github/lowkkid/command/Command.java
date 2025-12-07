package com.github.lowkkid.command;

import java.util.List;

@FunctionalInterface
public interface Command {
    void execute(List<String> args);

    default boolean shouldBreak() {
        return false;
    }
}
