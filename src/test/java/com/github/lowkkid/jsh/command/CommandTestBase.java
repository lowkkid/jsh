package com.github.lowkkid.jsh.command;

import com.github.lowkkid.jsh.Main;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.file.Path;

/**
 * Base class for command unit tests.
 * Provides utilities for capturing stdout/stderr and managing test state.
 */
public abstract class CommandTestBase {

    protected ByteArrayOutputStream stdOutCapture;
    protected ByteArrayOutputStream stdErrCapture;
    protected PrintStream testStdOut;
    protected PrintStream testStdErr;

    private Path originalCurrentDir;

    @BeforeEach
    void setUpStreams() {
        stdOutCapture = new ByteArrayOutputStream();
        stdErrCapture = new ByteArrayOutputStream();
        testStdOut = new PrintStream(stdOutCapture);
        testStdErr = new PrintStream(stdErrCapture);

        originalCurrentDir = Main.currentDir;
    }

    @AfterEach
    void restoreState() {
        Main.currentDir = originalCurrentDir;
    }

    /**
     * Injects custom PrintStreams into a Command instance via reflection.
     */
    protected void injectStreams(Command command) {
        try {
            Field stdOutField = Command.class.getDeclaredField("stdOut");
            Field stdErrField = Command.class.getDeclaredField("stdErr");
            stdOutField.setAccessible(true);
            stdErrField.setAccessible(true);
            stdOutField.set(command, testStdOut);
            stdErrField.set(command, testStdErr);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to inject streams", e);
        }
    }

    protected String getStdOut() {
        testStdOut.flush();
        return stdOutCapture.toString();
    }

    protected String getStdErr() {
        testStdErr.flush();
        return stdErrCapture.toString();
    }

    protected String getStdOutTrimmed() {
        return getStdOut().trim();
    }

    protected String getStdErrTrimmed() {
        return getStdErr().trim();
    }
}
