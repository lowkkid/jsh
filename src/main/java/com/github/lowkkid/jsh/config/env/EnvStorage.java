package com.github.lowkkid.jsh.config.env;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Two-tier storage for shell variables.
 *
 * <p>Variables live in one of two maps:
 * <ul>
 *   <li>{@code shellVars} — local shell variables, not inherited by child processes.</li>
 *   <li>{@code exportedVars} — exported (environment) variables, inherited by child processes.
 *       Initialized with {@link System#getenv()}.</li>
 * </ul>
 *
 * <p>Lookup priority: {@code shellVars} first, then {@code exportedVars}.
 * A variable exists in at most one map at any given time.
 *
 * <h3>Behavior summary</h3>
 * <table>
 *   <tr><th>Action</th><th>Result</th></tr>
 *   <tr><td>{@code set foo=1} then {@code set foo=2}</td>
 *       <td>Value updated in whichever map {@code foo} already resides in.
 *           If {@code foo} is new, it goes to {@code shellVars}.</td></tr>
 *   <tr><td>{@code set foo=1} then {@code export foo=2}</td>
 *       <td>{@code foo} removed from {@code shellVars}, placed into
 *           {@code exportedVars} with value {@code 2}.</td></tr>
 *   <tr><td>{@code export foo=1} then {@code set foo=2}</td>
 *       <td>Value updated in {@code exportedVars}; variable stays exported.</td></tr>
 *   <tr><td>{@code set foo=1} then {@code export foo}</td>
 *       <td>{@code foo} moved from {@code shellVars} to {@code exportedVars},
 *           keeping its current value.</td></tr>
 *   <tr><td>{@code export foo} (variable does not exist)</td>
 *       <td>Creates an exported variable with an empty string value</td></tr>
 *   <tr><td>{@code unset foo}</td>
 *       <td>Removed from both maps regardless of export status.</td></tr>
 * </table>
 */
public final class EnvStorage {

    private EnvStorage() {}

    private static final Map<String, String> exportedVars = new HashMap<>(System.getenv());
    private static final Map<String, String> shellVars = new HashMap<>();

    /**
     * Retrieves the value of a variable, checking {@code shellVars} first,
     * then {@code exportedVars}.
     *
     * @param key variable name
     * @return the value, or {@code null} if the variable does not exist
     */
    public static String get(String key) {
        String value = shellVars.get(key);
        return value != null ? value : exportedVars.get(key);
    }

    /**
     * Sets a variable as exported with the given value.
     * If the variable previously existed in {@code shellVars}, it is removed from there.
     *
     * <p>Equivalent to {@code export key=value} in bash/zsh.
     *
     * @param key   variable name
     * @param value variable value
     */
    public static void putExported(String key, String value) {
        shellVars.remove(key);
        exportedVars.put(key, value);
    }

    /**
     * Sets a shell variable. If the variable is already exported, the value is updated
     * in {@code exportedVars} and the variable remains exported (matching bash/zsh behavior).
     * Otherwise the value is written to {@code shellVars}.
     *
     * <p>Equivalent to {@code set key=value} in jsh.
     *
     * @param key   variable name
     * @param value variable value
     */
    public static void putShell(String key, String value) {
        if (exportedVars.containsKey(key)) {
            exportedVars.put(key, value);
        } else {
            shellVars.put(key, value);
        }
    }

    /**
     * Marks an existing shell variable as exported, moving it from {@code shellVars}
     * to {@code exportedVars}. If the variable does not exist in either map,
     * creates an exported variable with an empty string value (matching bash/zsh behavior).
     * If the variable is already exported, this is a no-op.
     *
     * <p>Equivalent to {@code export key} (without a value) in bash/zsh.
     *
     * @param key variable name
     */
    public static void markExported(String key) {
        String value = shellVars.remove(key);
        if (value != null) {
            exportedVars.put(key, value);
        } else if (!exportedVars.containsKey(key)) {
            exportedVars.put(key, "");
        }
    }

    /**
     * Returns an unmodifiable view of the exported variables map.
     * Used for building the environment of child processes ({@link ProcessBuilder}).
     *
     * @return unmodifiable view of exported variables
     */
    public static Map<String, String> getExportedVars() {
        return Collections.unmodifiableMap(exportedVars);
    }

    /**
     * Returns an unmodifiable view of the shell-only variables map.
     *
     * @return unmodifiable view of shell variables
     */
    public static Map<String, String> getShellVars() {
        return Collections.unmodifiableMap(shellVars);
    }

    /**
     * Removes a variable from both maps, regardless of its export status.
     *
     * @param key variable name
     */
    public static void delete(String key) {
        exportedVars.remove(key);
        shellVars.remove(key);
    }

    /**
     * Resets the storage to its initial state: clears all shell variables
     * and restores exported variables from {@link System#getenv()}.
     */
    public static void reset() {
        shellVars.clear();
        exportedVars.clear();
        exportedVars.putAll(System.getenv());
    }
}
