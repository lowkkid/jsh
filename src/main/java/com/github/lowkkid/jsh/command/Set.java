package com.github.lowkkid.jsh.command;

import com.github.lowkkid.jsh.config.env.EnvStorage;
import java.util.List;

/**
 * Built-in {@code set} command for shell variable assignment.
 *
 * <h3>Usage</h3>
 * <ul>
 *   <li>{@code set foo=value} — if {@code foo} is already exported, updates its value
 *       in the exported map (it stays exported). Otherwise creates/updates a shell-only
 *       variable.</li>
 *   <li>{@code set foo=1 bar=2} — multiple assignments are processed left to right.</li>
 *   <li>Arguments that are not valid assignments (no {@code =} or {@code =} at position 0)
 *       are silently skipped. Valid assignments in the same command are still processed.</li>
 * </ul>
 *
 * @see EnvStorage#putShell(String, String)
 */
public class Set extends Command {

    @Override
    protected void executeWithException(List<String> args) throws Exception {
        for (String arg : args) {
            int eqIndex = arg.indexOf('=');
            if (eqIndex <= 0) {
                continue;
            }
            String key = arg.substring(0, eqIndex);
            String value = arg.substring(eqIndex + 1);
            EnvStorage.putShell(key, value);
        }
    }
}
