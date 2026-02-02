package com.github.lowkkid.jsh.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExitTest extends CommandTestBase {

    private Exit exit;

    @BeforeEach
    void setUp() {
        exit = new Exit();
        injectStreams(exit);
    }

    @Test
    void shouldBreakReturnsTrue() {
        assertTrue(exit.shouldBreak());
    }

    @Test
    void executeProducesNoOutput() {
        exit.execute(List.of());

        assertTrue(getStdOut().isEmpty());
        assertTrue(getStdErr().isEmpty());
    }

    @Test
    void executeWithArgumentsProducesNoOutput() {
        exit.execute(List.of("0"));

        assertTrue(getStdOut().isEmpty());
        assertTrue(getStdErr().isEmpty());
    }
}
