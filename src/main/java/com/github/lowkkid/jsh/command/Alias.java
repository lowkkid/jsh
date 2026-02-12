package com.github.lowkkid.jsh.command;

import com.github.lowkkid.jsh.config.env.AliasStorage;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Built-in {@code alias} command for creating and displaying aliases.
 *
 * <h3>Usage</h3>
 * <ul>
 *   <li>{@code alias} — print all defined aliases.</li>
 *   <li>{@code alias name} — print the value of a specific alias.</li>
 *   <li>{@code alias name='value'} — create or update an alias.</li>
 * </ul>
 */
public class Alias extends Command {

    @Override
    protected void executeWithException(List<String> args) throws Exception {
        if (args.isEmpty()) {
            printAllAliases();
            return;
        }

        for (String arg : args) {
            int eqIndex = arg.indexOf('=');
            if (eqIndex == -1) {
                printAlias(arg);
            } else {
                String name = arg.substring(0, eqIndex);
                String value = arg.substring(eqIndex + 1);
                AliasStorage.put(name, value);
            }
        }
    }

    private void printAllAliases() {
        Map<String, String> sorted = new TreeMap<>(AliasStorage.getAll());
        for (var entry : sorted.entrySet()) {
            stdOut.println("alias " + entry.getKey() + "='" + entry.getValue() + "'");
        }
    }

    private void printAlias(String name) {
        String value = AliasStorage.get(name);
        if (value != null) {
            stdOut.println("alias " + name + "='" + value + "'");
        } else {
            stdErr.println("alias: " + name + ": not found");
        }
    }
}
