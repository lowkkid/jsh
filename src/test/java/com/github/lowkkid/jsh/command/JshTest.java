package com.github.lowkkid.jsh.command;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JshTest extends CommandTestBase {

    private Jsh jsh;

    @BeforeEach
    void setUp() {
        jsh = new Jsh();
        injectStreams(jsh);
    }

    @Test
    void outputContainsAsciiArt() {
        jsh.execute(List.of());

        String output = getStdOut();
        assertTrue(output.contains("██╗"));
        assertTrue(output.contains("╚═╝"));
    }

    @Test
    void outputContainsShellInfo() {
        jsh.execute(List.of());

        String output = getStdOut();
        assertTrue(output.contains("Shell"));
        assertTrue(output.contains("jsh"));
    }

    @Test
    void outputContainsJavaVersion() {
        jsh.execute(List.of());

        String output = getStdOut();
        assertTrue(output.contains("Java"));
        assertTrue(output.contains(System.getProperty("java.version")));
    }

    @Test
    void outputContainsOsInfo() {
        jsh.execute(List.of());

        String output = getStdOut();
        assertTrue(output.contains("OS"));
        assertTrue(output.contains(System.getProperty("os.name")));
    }

    @Test
    void outputContainsBuiltInsCount() {
        jsh.execute(List.of());

        String output = getStdOut();
        assertTrue(output.contains("Built-ins"));
    }

    @Test
    void loadVersionReturnsValue() {
        String version = Jsh.loadVersion();

        assertTrue(version != null && !version.isEmpty());
    }
}
