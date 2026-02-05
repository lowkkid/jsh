package com.github.lowkkid.jsh.executor;

import com.github.lowkkid.jsh.parser.CommandAndArgs;
import java.util.List;

public interface CommandExecutor {

    /**
     * Execute a single command (no pipeline).
     */
    ExecutionResult executeSingle(CommandAndArgs commandAndArgs);

    /**
     * Execute a pipeline of 2+ commands.
     */
    ExecutionResult executePipeline(List<CommandAndArgs> commandsAndArgs);

    record ExecutionResult(boolean shouldBreak) {}
}
