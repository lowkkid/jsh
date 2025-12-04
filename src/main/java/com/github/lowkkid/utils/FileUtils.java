package com.github.lowkkid.utils;

import com.github.lowkkid.Main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public final class FileUtils {

    private FileUtils() {}

    public static String existsInPathDirectories(String command) {
        String[] directoriesFromPathEnv = System.getenv("PATH").split(File.pathSeparator);

        for(var directoryFromPathEnv : directoriesFromPathEnv) {
            try (Stream<Path> contentOfDirectoryFromPathEnv = Files.list(Path.of(directoryFromPathEnv))) {
                var found =  contentOfDirectoryFromPathEnv.filter(Files::isExecutable)
                        .filter(path -> path.endsWith(command))
                        .findFirst();
                if (found.isPresent()) {
                    return found.get().toString();
                }
            } catch (IOException _) {
            }
        }
        return null;
    }

    public static String reduceToPath(List<String> dirs) {
        StringBuilder sb = new StringBuilder();
        for (String dir : dirs) {
            sb.append(dir).append("/");
        }
        return sb.toString();
    }
}
