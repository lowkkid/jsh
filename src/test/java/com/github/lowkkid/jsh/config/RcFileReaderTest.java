package com.github.lowkkid.jsh.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import com.github.lowkkid.jsh.Main;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RcFileReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void testEmptyFileReturnsEmptyList() throws IOException {
        Path rcFile = tempDir.resolve(".jshrc");
        Files.createFile(rcFile);

        List<String> commands = RcFileReader.getRcFileCommands(rcFile);

        assertTrue(commands.isEmpty());
    }

    @Test
    void testFileNotExistsReturnsEmptyList() throws IOException {
        Path rcFile = tempDir.resolve("nonexistent");

        List<String> commands = RcFileReader.getRcFileCommands(rcFile);

        assertTrue(commands.isEmpty());
    }

    @Test
    void testCommentLinesAreFiltered() throws IOException {
        Path rcFile = tempDir.resolve(".jshrc");
        Files.write(rcFile, List.of("# this is a comment", "  # indented comment"));

        List<String> commands = RcFileReader.getRcFileCommands(rcFile);

        assertTrue(commands.isEmpty());
    }

    @Test
    void testBlankLinesAreFiltered() throws IOException {
        Path rcFile = tempDir.resolve(".jshrc");
        Files.write(rcFile, List.of("", "   ", "\t"));

        List<String> commands = RcFileReader.getRcFileCommands(rcFile);

        assertTrue(commands.isEmpty());
    }

    @Test
    void testValidCommandsAreReturned() throws IOException {
        Path rcFile = tempDir.resolve(".jshrc");
        Files.write(rcFile, List.of("echo hello", "cd /tmp"));

        List<String> commands = RcFileReader.getRcFileCommands(rcFile);

        assertEquals(List.of("echo hello", "cd /tmp"), commands);
    }

    @Test
    void testMixedContent() throws IOException {
        Path rcFile = tempDir.resolve(".jshrc");
        Files.write(rcFile, List.of(
                "# comment",
                "",
                "echo hello",
                "  # another comment",
                "   ",
                "cd /tmp"
        ));

        List<String> commands = RcFileReader.getRcFileCommands(rcFile);

        assertEquals(List.of("echo hello", "cd /tmp"), commands);
    }
}
