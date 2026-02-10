package com.github.lowkkid.jsh.command;

import com.github.lowkkid.jsh.Main;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

public class ExternalCommand extends Command {

    private final String commandName;

    public ExternalCommand(String commandName) {
        this.commandName = commandName;
    }

    @Override
    public void executeWithException(List<String> args) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                Stream.concat(Stream.of(commandName), args.stream()).toList())
                .directory(Main.currentDir.toFile());
        if (isRedirected()) {
            pb.redirectErrorStream(false);
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stdOut.println(line);
                }
            }

            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    stdErr.println(line);
                }
            }
            process.waitFor();
        } else {
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();
        }
    }
}
