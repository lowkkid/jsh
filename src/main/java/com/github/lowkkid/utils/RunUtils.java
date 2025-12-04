package com.github.lowkkid.utils;

import com.github.lowkkid.exception.CommandNotFoundException;

import java.io.IOException;

public final class RunUtils {

    private RunUtils() {}

    public static void runExternal(String command, String[] args) {
        String cmdPath = FileUtils.existsInPathDirectories(command);
        if (cmdPath == null) {
            throw new CommandNotFoundException(command);
        }
        var commandAndArgs = new String[args.length + 1];
        commandAndArgs[0] = command;
        System.arraycopy(args, 0, commandAndArgs, 1, args.length);
        ProcessBuilder pb = new ProcessBuilder(commandAndArgs).inheritIO();
        try {
            Process process = pb.start();
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
