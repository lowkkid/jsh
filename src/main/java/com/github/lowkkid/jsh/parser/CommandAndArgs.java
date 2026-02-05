package com.github.lowkkid.jsh.parser;

import java.util.List;

public record CommandAndArgs(String command, List<String> arguments, RedirectOptions redirectOptions) {

    public boolean shouldBeRedirected() {
        return redirectOptions != null;
    }

    @Override
    public String toString() {
        return "'" + command + "', args: '" + String.join(",", arguments) + "'";
    }
}
