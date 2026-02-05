package com.github.lowkkid.jsh.executor;

import com.github.lowkkid.jsh.command.Command;
import com.github.lowkkid.jsh.parser.CommandAndArgs;
import java.util.List;

/**
 * Represents a segment of a command pipeline.
 *
 * <p>A pipeline is split into segments at built-in command boundaries.
 * Each segment is either a single built-in command or a sequence of
 * external commands that can be executed together via
 * {@link ProcessBuilder#startPipeline(List)}.
 *
 * <p>Example pipeline: {@code cat file | grep foo | pwd | wc -l | head}
 * <pre>
 * Segments:
 *   1. ExternalSegment [cat file, grep foo]
 *   2. BuiltInSegment  [pwd]
 *   3. ExternalSegment [wc -l, head]
 * </pre>
 */
public sealed interface PipelineSegment permits
        PipelineSegment.BuiltInSegment,
        PipelineSegment.ExternalSegment {

    /**
     * A segment containing a single built-in command.
     *
     * @param command the built-in command instance
     * @param args    command arguments
     */
    record BuiltInSegment(Command command, List<String> args) implements PipelineSegment {}

    /**
     * A segment containing one or more external commands.
     *
     * <p>These commands will be executed together using
     * {@link ProcessBuilder#startPipeline(List)}, allowing the OS kernel
     * to handle piping between them efficiently.
     *
     * @param commands list of external commands with their arguments
     */
    record ExternalSegment(List<CommandAndArgs> commands) implements PipelineSegment {}
}
