package com.github.lowkkid.jsh.command.utils;

import static com.github.lowkkid.jsh.config.EnvConfigReader.FILE_HISTORY_MAX_ENTRIES;
import static com.github.lowkkid.jsh.config.EnvConfigReader.HISTORY_FILE;
import static com.github.lowkkid.jsh.config.EnvConfigReader.INCREMENTAL_APPEND_HISTORY;
import static com.github.lowkkid.jsh.config.EnvConfigReader.IN_MEMORY_HISTORY_MAX_ENTRIES;

import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.history.DefaultHistory;

public final class HistoryUtils {

    private HistoryUtils() {}

    public static final History HISTORY;
    public static int initialHistorySize = 0;

    static {
        HISTORY = new DefaultHistory();
    }

    public static void configureHistory(LineReaderBuilder readerBuilder) {
        readerBuilder.history(HISTORY)
                .variable(LineReader.HISTORY_FILE, HISTORY_FILE)
                .variable(LineReader.HISTORY_SIZE, IN_MEMORY_HISTORY_MAX_ENTRIES)
                .variable(LineReader.HISTORY_FILE_SIZE, FILE_HISTORY_MAX_ENTRIES)
                .option(LineReader.Option.HISTORY_INCREMENTAL, INCREMENTAL_APPEND_HISTORY)
                .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true);
    }

    public static void afterInitialization() {
        initialHistorySize = HISTORY.size();
        com.github.lowkkid.jsh.command.History.initLastAppendedIndex();
    }
}
