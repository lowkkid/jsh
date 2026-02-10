package com.github.lowkkid.jsh.command;

import static com.github.lowkkid.jsh.utils.FileUtils.createParentDirsIfNotExists;

import com.github.lowkkid.jsh.Main;
import com.github.lowkkid.jsh.parser.RedirectOptions;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class Command {

    protected PrintStream stdOut = System.out;
    protected PrintStream stdErr = System.err;
    protected BufferedReader stdIn = null;

    public void execute(List<String> args) {
        try {
            executeWithException(args);
        } catch (Exception e) {
            stdErr.println(e.getMessage());
        }
    }

    protected abstract void executeWithException(List<String> args) throws Exception;

    public boolean shouldBreak() {
        return false;
    }

    public void executeWithRedirect(List<String> args, RedirectOptions redirectOptions) {
        var originalStdOut = stdOut;
        var originalStdErr = stdErr;
        var redirectTo = Main.currentDir.resolve(redirectOptions.redirectTo()).normalize();
        createParentDirsIfNotExists(redirectTo);
        try (PrintStream fileOutput = new PrintStream(
                new BufferedOutputStream(
                        new FileOutputStream(redirectTo.toFile(), redirectOptions.isAppending()),
                        8192
                ),
                false,
                StandardCharsets.UTF_8)) {

            if (redirectOptions.isRedirectingStdOut()) {
                stdOut = fileOutput;
            } else {
                stdErr = fileOutput;
            }

            execute(args);

        } catch (IOException e) {
            stdErr.println("Redirect error: " + e.getMessage());
        } finally {
            resetStdOut(originalStdOut);
            resetStdErr(originalStdErr);
        }
    }

    /**
     * Executes the command within a pipeline context.
     *
     * <p>This method sets up stdin and stdout streams for pipeline execution,
     * allowing the command to read from the previous segment's output and write
     * to the next segment's input.
     *
     * @param stdin  input stream from previous pipeline segment (may be null)
     * @param stdout output stream to next pipeline segment
     * @param args   command arguments
     */
    public void executeInPipeline(InputStream stdin, OutputStream stdout, List<String> args) {
        var originalStdOut = stdOut;
        var originalStdIn = stdIn;

        stdOut = new PrintStream(stdout, true, StandardCharsets.UTF_8);
        if (stdin != null) {
            stdIn = new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8));
        }

        try {
            execute(args);
        } finally {
            stdOut = originalStdOut;
            stdIn = originalStdIn;
        }
    }

    protected void resetStdOut(PrintStream original) {
        this.stdOut = original;
    }

    protected void resetStdErr(PrintStream original) {
        this.stdErr = original;
    }

    /**
     * Returns the stdin reader for use in pipeline context.
     * May be null if command is not executing within a pipeline or is the first command.
     */
    protected BufferedReader getStdIn() {
        return stdIn;
    }

    protected boolean isRedirected() {
        return stdOut != System.out || stdErr != System.err;
    }
}
