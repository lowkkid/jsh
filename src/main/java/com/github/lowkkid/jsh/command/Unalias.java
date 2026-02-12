package com.github.lowkkid.jsh.command;

import com.github.lowkkid.jsh.config.env.AliasStorage;
import java.util.List;

/**
 * Built-in {@code unalias} command for removing aliases.
 *
 * <h3>Usage</h3>
 * <ul>
 *   <li>{@code unalias name} — remove an alias.</li>
 *   <li>{@code unalias name1 name2} — remove multiple aliases.</li>
 * </ul>
 */
public class Unalias extends Command {

    @Override
    protected void executeWithException(List<String> args) throws Exception {
        if (args.isEmpty()) {
            stdErr.println("unalias: usage: unalias name [name ...]");
            return;
        }

        for (String name : args) {
            if (!AliasStorage.remove(name)) {
                stdErr.println("unalias: " + name + ": not found");
            }
        }
    }
}
