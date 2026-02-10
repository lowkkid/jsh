package com.github.lowkkid.jsh.config;

import static com.github.lowkkid.jsh.config.EnvConfigReader.HOME;

import com.github.lowkkid.jsh.Main;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public final class RcFileReader {

    private RcFileReader() {}

    public static void executeRcFileCommands() throws IOException {
        var commands = getRcFileCommands();
        commands.forEach(Main::parseAndExecute);
    }

    private static List<String> getRcFileCommands() throws IOException {
        var rcFilePath = Paths.get(HOME + "/.jshrc");

        return Files.readAllLines(rcFilePath).stream()
                .filter(s -> !s.trim().startsWith("#"))
                .toList();

    }
}
