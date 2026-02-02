package com.github.lowkkid.jsh.command.utils;

import com.github.lowkkid.jsh.command.Cd;
import com.github.lowkkid.jsh.command.Command;
import com.github.lowkkid.jsh.command.Echo;
import com.github.lowkkid.jsh.command.Exit;
import com.github.lowkkid.jsh.command.ExternalCommand;
import com.github.lowkkid.jsh.command.History;
import com.github.lowkkid.jsh.command.Pwd;
import com.github.lowkkid.jsh.command.Type;
import com.github.lowkkid.jsh.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Singleton registry for shell commands (both built-in and external).
 *
 * <p>This class manages command registration, caching, and lookup for the shell.
 * Built-in commands are registered manually at startup, external commands found
 * in PATH are lazily cached.
 *
 * <h2>Caching Strategy</h2>
 * <p>External commands are cached using an LRU (Least Recently Used) eviction policy
 * with a maximum capacity of {@value #MAX_EXTERNAL_COMMANDS_CACHE} entries.
 * Built-in commands are never evicted from the cache.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * CommandRegistry registry = CommandRegistry.getInstance();
 *
 * // Lookup a command (returns cached or creates new)
 * Optional<Command> cmd = registry.getExecutableCommand("ls");
 *
 * // Check if command is built-in
 * boolean isBuiltIn = registry.isBultInCommand("cd");
 *
 * // Get all available commands for autocompletion
 * Set<String> allCommands = registry.getAllCommands();
 * }</pre>
 *
 * @see Command
 * @see ExternalCommand
 */
public class CommandRegistry {

    /**
     * Maximum number of external commands to keep in cache.
     * When exceeded, least recently used external commands are evicted.
     */
    private static final int MAX_EXTERNAL_COMMANDS_CACHE = 100;

    /**
     * Returns the singleton instance of the command registry.
     *
     * <p>Uses initialization-on-demand holder idiom for thread-safe lazy initialization.
     *
     * @return the singleton {@code CommandRegistry} instance
     */
    public static CommandRegistry getInstance() {
        return CommandRegistryHolder.INSTANCE;
    }

    /**
     * LRU cache of executable commands (both built-in and external).
     * Uses access-order LinkedHashMap with automatic eviction of eldest external commands.
     */
    private final Map<String, Command> executableCommands;

    /** Set of built-in command names (protected from cache eviction). */
    private final Set<String> builtInCommands;

    /**
     * Private constructor - initializes the command cache and discovers built-in commands.
     *
     * <p>The cache is implemented as a {@link LinkedHashMap} with:
     * <ul>
     *   <li>Access-order iteration (most recently accessed elements at the end)</li>
     *   <li>Automatic eviction of least recently used external commands when size
     *       exceeds {@value #MAX_EXTERNAL_COMMANDS_CACHE}</li>
     *   <li>Protection for built-in commands (never evicted)</li>
     * </ul>
     */
    private CommandRegistry() {
        this.builtInCommands = new HashSet<>();
        this.executableCommands = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Command> eldest) {
                return size() > MAX_EXTERNAL_COMMANDS_CACHE
                        && !builtInCommands.contains(eldest.getKey());
            }
        };
        registerBuiltInCommands();
    }

    /**
     * Retrieves a command by name, loading it into cache if necessary.
     *
     * <p>Lookup order:
     * <ol>
     *   <li>Check the internal cache</li>
     *   <li>If command is not found in cache, search for executable in PATH directories</li>
     *   <li>If found, create {@link ExternalCommand} and cache it</li>
     * </ol>
     *
     * <p>Accessing a cached command updates its position in the LRU order.
     *
     * @param name the command name to look up (e.g., "ls", "cd", "grep")
     * @return an {@link Optional} containing the command if found, or empty if not found
     */
    public Optional<Command> getExecutableCommand(String name) {
        return Optional.ofNullable(executableCommands.get(name))
                .or(() -> {
                    var externalPath = FileUtils.existsInPathDirectories(name);
                    if (externalPath != null) {
                        var externalExecutableCommand = new ExternalCommand(name);
                        putExecutableCommand(name, externalExecutableCommand);
                        return Optional.of(externalExecutableCommand);

                    }
                    return Optional.empty();
                });
    }

    /**
     * Checks if the given command is a built-in shell command.
     *
     * <p>Built-in commands are implemented in Java and registered in {@link #registerBuiltInCommands()}.
     * They have special handling (e.g., never evicted from cache, can modify shell state).
     *
     * @param command the command name to check
     * @return {@code true} if the command is built-in, {@code false} otherwise
     */
    public boolean isBultInCommand(String command) {
        return builtInCommands.contains(command);
    }

    /**
     * Returns all available commands (built-in + all executables in PATH).
     *
     * <p>This method scans all directories in the PATH environment variable
     * and collects names of executable files. Primarily used for tab-completion.
     *
     * <p><b>Note:</b> This is an expensive operation as it performs filesystem I/O.
     * Results are not cached.
     *
     * @return a set of all available command names
     */
    public Set<String> getAllCommands() {
        Set<String> allCommands = new HashSet<>(builtInCommands);
        String[] directoriesFromPathEnv = System.getenv("PATH").split(File.pathSeparator);

        for (var directoryFromPathEnv : directoriesFromPathEnv) {
            try (Stream<Path> contentOfDirectoryFromPathEnv = Files.list(Path.of(directoryFromPathEnv))) {
                var found = contentOfDirectoryFromPathEnv
                        .filter(Files::isExecutable)
                        .map(path ->  path.getFileName().toString())
                        .collect(Collectors.toSet());
                allCommands.addAll(found);
            } catch (IOException _) {
            }
        }
        return allCommands;
    }

    /**
     * Adds a command to the cache.
     *
     * @param name    the command name
     * @param command the command instance
     */
    private void putExecutableCommand(String name, Command command) {
        if (executableCommands.containsKey(name)) {
            return;
        }
        executableCommands.put(name, command);
    }

    /**
     * Registers all built-in commands manually.
     */
    private void registerBuiltInCommands() {
        registerBuiltIn("echo", new Echo());
        registerBuiltIn("exit", new Exit());
        registerBuiltIn("cd", new Cd());
        registerBuiltIn("pwd", new Pwd());
        registerBuiltIn("type", new Type(this));
        registerBuiltIn("history", new History());
    }

    private void registerBuiltIn(String name, Command command) {
        putExecutableCommand(name, command);
        builtInCommands.add(name);
    }

    /**
     * Initialization-on-demand holder for thread-safe lazy singleton.
     * The inner class is not loaded until {@link #getInstance()} is called.
     */
    private static class CommandRegistryHolder {
        private static final CommandRegistry INSTANCE = new CommandRegistry();
    }
}
