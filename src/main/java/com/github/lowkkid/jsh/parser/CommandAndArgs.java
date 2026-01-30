package com.github.lowkkid.jsh.parser;

import java.util.List;


public class CommandAndArgs {

    private final String command;
    private final List<String> arguments;
    private RedirectOptions redirectOptions;

    public CommandAndArgs(String command, List<String> arguments) {
        this.command = command;
        this.arguments = arguments;
    }

    public CommandAndArgs(String command, List<String> arguments, RedirectOptions redirectOptions) {
        this.command = command;
        this.arguments = arguments;
        this.redirectOptions = redirectOptions;
    }

    public boolean shouldBeRedirected() {
        return redirectOptions != null;
    }

    public String getCommand() {
        return command;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public RedirectOptions getRedirectOptions() {
        return redirectOptions;
    }
}
