package com.github.lowkkid.jsh.command;

import com.github.lowkkid.jsh.Main;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CdTest extends CommandTestBase {

    private Cd cd;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        cd = new Cd();
        injectStreams(cd);
        Main.currentDir = tempDir;
    }

    @Test
    void absolutePath() throws IOException {
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectory(subDir);

        cd.execute(List.of(subDir.toString()));

        assertEquals(subDir, Main.currentDir);
        assertTrue(getStdErr().isEmpty());
    }

    @Test
    void relativePath() throws IOException {
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectory(subDir);

        cd.execute(List.of("subdir"));

        assertEquals(subDir, Main.currentDir);
    }

    @Test
    void nestedRelativePath() throws IOException {
        Path nested = tempDir.resolve("a/b/c");
        Files.createDirectories(nested);

        cd.execute(List.of("a/b/c"));

        assertEquals(nested, Main.currentDir);
    }

    @Test
    void parentDirectory() throws IOException {
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectory(subDir);
        Main.currentDir = subDir;

        cd.execute(List.of(".."));

        assertEquals(tempDir, Main.currentDir);
    }

    @Test
    void currentDirectory() {
        Path original = Main.currentDir;

        cd.execute(List.of("."));

        assertEquals(original, Main.currentDir);
    }

    @Test
    void combinedDotDotAndPath() throws IOException {
        Path dirA = tempDir.resolve("a");
        Path dirB = tempDir.resolve("b");
        Files.createDirectories(dirA);
        Files.createDirectories(dirB);
        Main.currentDir = dirA;

        cd.execute(List.of("../b"));

        assertEquals(dirB, Main.currentDir);
    }

    @Test
    void homeDirectory() {
        String home = System.getenv("HOME");
        if (home != null && Files.isDirectory(Path.of(home))) {
            cd.execute(List.of("~"));
            assertEquals(Path.of(home), Main.currentDir);
        }
    }

    @Test
    void nonExistentDirectory() {
        Path original = Main.currentDir;

        cd.execute(List.of("nonexistent"));

        assertEquals(original, Main.currentDir);
        assertTrue(getStdErrTrimmed().contains("No such file or directory"));
    }

    @Test
    void fileInsteadOfDirectory() throws IOException {
        Path file = tempDir.resolve("file.txt");
        Files.createFile(file);
        Path original = Main.currentDir;

        cd.execute(List.of("file.txt"));

        assertEquals(original, Main.currentDir);
        assertTrue(getStdErrTrimmed().contains("No such file or directory"));
    }

    @Test
    void multipleDotDot() throws IOException {
        Path deep = tempDir.resolve("a/b/c");
        Files.createDirectories(deep);
        Main.currentDir = deep;

        cd.execute(List.of("../../.."));

        assertEquals(tempDir, Main.currentDir);
    }
}
