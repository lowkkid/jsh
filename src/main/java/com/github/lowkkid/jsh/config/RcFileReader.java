package com.github.lowkkid.jsh.config;

import static com.github.lowkkid.jsh.config.env.EnvConfigReader.HOME;

import com.github.lowkkid.jsh.Main;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class RcFileReader {

    private RcFileReader() {}

    public static void executeRcFileCommands() throws IOException {
        var commands = getRcFileCommands();
        commands.forEach(Main::parseAndExecute);
    }

    static List<String> getRcFileCommands() throws IOException {
        return getRcFileCommands(Path.of(HOME + "/.jshrc"));
    }

    static List<String> getRcFileCommands(Path rcFilePath) throws IOException {
        if (!Files.exists(rcFilePath)) {
            return List.of();
        }

        return Files.readAllLines(rcFilePath).stream()
                .filter(s -> !s.trim().startsWith("#") && !s.isBlank())
                .toList();
    }
}
