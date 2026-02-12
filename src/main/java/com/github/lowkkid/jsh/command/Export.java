package com.github.lowkkid.jsh.command;

import com.github.lowkkid.jsh.config.env.EnvStorage;
import java.util.List;

/**
 * Built-in {@code export} command. Marks variables as exported (inherited by child processes)
 * and optionally assigns values.
 *
 * <h3>Supported forms</h3>
 * <ul>
 *   <li>{@code export foo=value} — sets and exports {@code foo} with the given value.
 *       If {@code foo} was a shell-only variable, it becomes exported with specified value.
 *       If {@code foo} was already exported, the value is overwritten.</li>
 *   <li>{@code export foo} — exports {@code foo} without changing its value.
 *       If {@code foo} was a shell-only variable, it is moved to the exported map.
 *       If {@code foo} does not exist, it is created as an exported variable
 *       with an empty string value.</li>
 *   <li>{@code export foo=1 bar baz=2} — multiple arguments are processed left to right,
 *       each following the rules above.</li>
 * </ul>
 */
public class Export extends Command {

    @Override
    protected void executeWithException(List<String> args) throws Exception {
        for (String arg : args) {
            int eqIndex = arg.indexOf('=');
            if (eqIndex == -1) {
                EnvStorage.markExported(arg);
            } else if (eqIndex > 0) {
                String key = arg.substring(0, eqIndex);
                String value = arg.substring(eqIndex + 1);
                EnvStorage.putExported(key, value);
            }
        }
    }
}
