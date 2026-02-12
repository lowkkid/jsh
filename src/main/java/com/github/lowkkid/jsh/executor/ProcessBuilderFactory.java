package com.github.lowkkid.jsh.executor;

import com.github.lowkkid.jsh.Main;
import com.github.lowkkid.jsh.config.env.EnvStorage;
import java.util.List;

/**
 * Factory for creating {@link ProcessBuilder} instances with the shell's
 * exported environment and current working directory.
 *
 * <p>By default, {@code ProcessBuilder} inherits {@link System#getenv()} of the JVM process.
 * This factory replaces that with the shell's own exported variables from {@link EnvStorage},
 * so that {@code set}, {@code export}, and {@code unset} changes are visible to child processes.
 */
public final class ProcessBuilderFactory {

    private ProcessBuilderFactory() {}

    /**
     * Creates a {@link ProcessBuilder} with the shell's exported environment
     * and {@link Main#currentDir} as the working directory.
     *
     * @param command the command and its arguments
     * @return a configured {@link ProcessBuilder}
     */
    public static ProcessBuilder create(List<String> command) {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(Main.currentDir.toFile());
        pb.environment().clear();
        pb.environment().putAll(EnvStorage.getExportedVars());
        return pb;
    }

    /**
     * Creates a {@link ProcessBuilder} with the shell's exported environment
     * and {@link Main#currentDir} as the working directory.
     *
     * @param command the command and its arguments
     * @return a configured {@link ProcessBuilder}
     */
    public static ProcessBuilder create(String... command) {
        return create(List.of(command));
    }
}
