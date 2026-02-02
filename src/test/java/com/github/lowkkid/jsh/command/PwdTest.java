package com.github.lowkkid.jsh.command;

import com.github.lowkkid.jsh.Main;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PwdTest extends CommandTestBase {

    private Pwd pwd;

    @BeforeEach
    void setUp() {
        pwd = new Pwd();
        injectStreams(pwd);
    }

    @Test
    void printsCurrentDirectory() {
        Main.currentDir = Path.of("/home/user");

        pwd.execute(List.of());

        assertEquals("/home/user", getStdOutTrimmed());
        assertTrue(getStdErr().isEmpty());
    }

    @Test
    void printsRootDirectory() {
        Main.currentDir = Path.of("/");

        pwd.execute(List.of());

        assertEquals("/", getStdOutTrimmed());
    }

    @Test
    void printsNestedDirectory() {
        Main.currentDir = Path.of("/home/user/projects/jsh");

        pwd.execute(List.of());

        assertEquals("/home/user/projects/jsh", getStdOutTrimmed());
    }

    @Test
    void ignoresArguments() {
        Main.currentDir = Path.of("/tmp");

        pwd.execute(List.of("ignored", "arguments"));

        assertEquals("/tmp", getStdOutTrimmed());
    }

    @Test
    void outputEndsWithNewline() {
        Main.currentDir = Path.of("/tmp");

        pwd.execute(List.of());

        assertTrue(getStdOut().endsWith("\n") || getStdOut().endsWith(System.lineSeparator()));
    }
}
