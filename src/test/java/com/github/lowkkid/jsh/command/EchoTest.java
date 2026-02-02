package com.github.lowkkid.jsh.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EchoTest extends CommandTestBase {

    private Echo echo;

    @BeforeEach
    void setUp() {
        echo = new Echo();
        injectStreams(echo);
    }

    @Test
    void singleArgument() {
        echo.execute(List.of("hello"));

        assertEquals("hello", getStdOutTrimmed());
        assertTrue(getStdErr().isEmpty());
    }

    @Test
    void multipleArguments() {
        echo.execute(List.of("hello", "world"));

        assertEquals("hello world", getStdOutTrimmed());
    }

    @Test
    void emptyArguments() {
        echo.execute(List.of());

        assertEquals("", getStdOutTrimmed());
    }

    @Test
    void argumentsWithSpecialCharacters() {
        echo.execute(List.of("hello!", "@#$%", "test123"));

        assertEquals("hello! @#$% test123", getStdOutTrimmed());
    }

    @Test
    void argumentsWithSpacesPreserved() {
        // arguments are already parsed, so spaces within args are preserved
        echo.execute(List.of("hello world", "foo bar"));

        assertEquals("hello world foo bar", getStdOutTrimmed());
    }

    @Test
    void outputEndsWithNewline() {
        echo.execute(List.of("test"));

        assertTrue(getStdOut().endsWith("\n")
                || getStdOut().endsWith(System.lineSeparator()));
    }
}
