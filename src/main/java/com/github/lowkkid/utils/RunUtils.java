package com.github.lowkkid.utils;

import com.github.lowkkid.exception.CommandNotFoundException;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

public final class RunUtils {

    private RunUtils() {}

    public static void runExternal(String command, List<String> args) {
        String cmdPath = FileUtils.existsInPathDirectories(command);
        if (cmdPath == null) {
            throw new CommandNotFoundException(command);
        }
        ProcessBuilder pb = new ProcessBuilder(
                Stream.concat(Stream.of(command), args.stream()).toList())
                .inheritIO();
        try {
            Process process = pb.start();
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
