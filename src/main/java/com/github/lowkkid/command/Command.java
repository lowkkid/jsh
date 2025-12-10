package com.github.lowkkid.command;

import com.github.lowkkid.Main;
import com.github.lowkkid.parser.RedirectOptions;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.github.lowkkid.utils.FileUtils.createParentDirsIfNotExists;

public abstract class Command {

    protected PrintStream stdOut = System.out;
    protected PrintStream stdErr = System.out;

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
        var redirectTo = Main.currentDir.resolve(redirectOptions.getRedirectTo()).normalize();
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

    protected void resetStdOut(PrintStream original) {
        this.stdOut = original;
    }

    protected void resetStdErr(PrintStream original) {
        this.stdErr = original;
    }
}
