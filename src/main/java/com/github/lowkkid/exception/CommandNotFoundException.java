package com.github.lowkkid.exception;

public class CommandNotFoundException extends RuntimeException {
    public CommandNotFoundException(String command) {
        super(command + ": not found");
    }
}
