package com.github.lowkkid.jsh.command;

import com.github.lowkkid.jsh.Main;
import com.github.lowkkid.jsh.utils.FileUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Cd extends Command {

    @Override
    public void executeWithException(List<String> args) {
        String dirArg = args.getFirst();
        if (dirArg.startsWith("/")) {
            updateCurrentDirectory(dirArg);
            return;
        } else if (dirArg.startsWith("~")) {
            updateCurrentDirectory(System.getenv("HOME"));
            return;
        }


        var currentDirectories = new LinkedList<>(Arrays.asList(Main.currentDir.toString().split("/")));
        for (var dirMove : dirArg.split("/")) {
            switch (dirMove) {
                case "." -> {  }
                case ".." -> currentDirectories.removeLast();
                default -> currentDirectories.add(dirMove);
            }
        }
        var newDir = FileUtils.reduceToPath(currentDirectories);
        updateCurrentDirectory(newDir);

    }

    private void updateCurrentDirectory(String newDir) {
        Path newPath = Path.of(newDir);

        if (!Files.isDirectory(newPath)) {
            stdErr.println("cd: " + newPath + ": No such file or directory");
        } else {
            Main.currentDir = newPath;
        }
    }
}
