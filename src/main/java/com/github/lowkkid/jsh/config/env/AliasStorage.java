package com.github.lowkkid.jsh.config.env;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Static storage for shell aliases.
 *
 * <p>An alias maps a short name to a command string that may include
 * arguments, pipes, and redirects. Aliases are expanded before input parsing.
 *
 * @see com.github.lowkkid.jsh.parser.InputParser
 */
public final class AliasStorage {

    private AliasStorage() {}

    private static final Map<String, String> aliases = new HashMap<>();

    /**
     * Retrieves the value of an alias.
     *
     * @param name alias name
     * @return the alias value, or {@code null} if not defined
     */
    public static String get(String name) {
        return aliases.get(name);
    }

    /**
     * Creates or updates an alias.
     *
     * @param name  alias name
     * @param value the command string to expand to
     */
    public static void put(String name, String value) {
        aliases.put(name, value);
    }

    /**
     * Removes an alias.
     *
     * @param name alias name
     * @return {@code true} if the alias existed and was removed
     */
    public static boolean remove(String name) {
        return aliases.remove(name) != null;
    }

    /**
     * Checks whether an alias is defined.
     *
     * @param name alias name
     * @return {@code true} if the alias exists
     */
    public static boolean contains(String name) {
        return aliases.containsKey(name);
    }

    /**
     * Returns an unmodifiable view of all aliases.
     *
     * @return unmodifiable map of alias name to value
     */
    public static Map<String, String> getAll() {
        return Collections.unmodifiableMap(aliases);
    }

    /**
     * Removes all aliases.
     */
    public static void reset() {
        aliases.clear();
    }
}
