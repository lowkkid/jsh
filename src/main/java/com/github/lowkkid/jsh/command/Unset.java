package com.github.lowkkid.jsh.command;

import com.github.lowkkid.jsh.config.env.EnvStorage;
import java.util.List;

/**
 * Built-in {@code unset} command. Removes variables from the shell,
 * regardless of whether they are shell-only or exported.
 *
 * <h3>Usage</h3>
 * <ul>
 *   <li>{@code unset foo} — removes {@code foo} from both maps.</li>
 *   <li>{@code unset foo bar baz} — removes multiple variables.</li>
 * </ul>
 *
 * @see EnvStorage#delete(String)
 */
public class Unset extends Command {

    @Override
    protected void executeWithException(List<String> args) throws Exception {
        for (String arg : args) {
            EnvStorage.delete(arg);
        }
    }
}
