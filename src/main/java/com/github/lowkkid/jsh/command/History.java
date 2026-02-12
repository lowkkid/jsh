package com.github.lowkkid.jsh.command;

import static com.github.lowkkid.jsh.command.utils.HistoryUtils.HISTORY;
import static com.github.lowkkid.jsh.command.utils.HistoryUtils.initialHistorySize;
import static com.github.lowkkid.jsh.config.env.EnvConfigReader.FILE_HISTORY_MAX_ENTRIES;
import static com.github.lowkkid.jsh.config.env.EnvConfigReader.IN_MEMORY_HISTORY_MAX_ENTRIES;
import static com.github.lowkkid.jsh.utils.StringUtils.isInteger;

import com.github.lowkkid.jsh.config.env.EnvConfigReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Implementation of the {@code history} built-in command, similar to bash/zsh.
 *
 * <h2>Overview</h2>
 * This command manages the command history list - a record of commands entered during
 * shell sessions. The history is stored both in memory (for the current session) and
 * can be persisted to/loaded from files.
 *
 * <h2>Configuration</h2>
 * <ul>
 *   <li>{@code IN_MEMORY_HISTORY_MAX_ENTRIES} - Maximum entries kept in memory (like HISTSIZE in bash)</li>
 *   <li>{@code FILE_HISTORY_MAX_ENTRIES} - Maximum entries written to file (like HISTFILESIZE in bash).
 *       Applied only during {@code -w} operation - older entries are truncated.</li>
 * </ul>
 *
 * @see com.github.lowkkid.jsh.command.utils.HistoryUtils
 * @see EnvConfigReader
 */
public class History extends Command {

    private static final String MSG_INVALID_NUMBER = "history: invalid number: %s";
    private static final String MSG_INVALID_OPTION = "history: %s: invalid option";
    private static final String MSG_WRITTEN_TO_FILE = "history: written history to %s";
    private static final String MSG_NO_NEW_COMMANDS = "history: no new commands to append";
    private static final String MSG_APPENDED_TO_FILE = "history: appended %d entries to %s";
    private static final String MSG_FILE_NOT_FOUND = "history: file not found: %s";
    private static final String MSG_MEMORY_LIMIT_REACHED = "history: memory limit reached, stopped loading";
    private static final String MSG_LOADED_FROM_FILE = "history: loaded %d entries from %s";

    /**
     * Tracks how many history entries have been written to file.
     *
     * <p>
     * This counter is crucial for the {@code -a} (append) functionality:
     * <ul>
     *   <li>Initialized to {@code initialHistorySize} at startup (entries loaded from default file)</li>
     *   <li>Updated after each write/append/read operation</li>
     *   <li>Reset to 0 after {@code -c} (clear)</li>
     * </ul>
     * </p>
     */
    private static int historyWrittenCount = -1;

    /**
     * Initializes {@code historyWrittenCount} after the default history file is loaded.
     *
     * <p>
     * Must be called once after JLine loads the history file at startup.
     * This ensures that entries loaded from the default file are not re-appended
     * when {@code history -a} is called.
     * </p>
     */
    public static void initLastAppendedIndex() {
        historyWrittenCount = initialHistorySize;
    }

    /**
     * Executes the history command with the given arguments.
     *
     * <h4>Supported Arguments</h4>
     * <ul>
     *   <li>No args - Display all history entries with line numbers</li>
     *   <li>{@code N} - Display the last N entries (like bash)</li>
     *   <li>{@code -r [file]} - Read history from file and append to current history.
     *       If no filename is provided, the default history file is used</li>
     *   <li>{@code -w [file]} - Write current history to file (overwrites).
     *       If no filename is provided, the default history file is used</li>
     *   <li>{@code -a [file]} - Append new entries (since last write) to file.
     *       If no filename is provided, the default history file is used</li>
     *   <li>{@code -c} - Clear the history list</li>
     * </ul>
     *
     * @param args command arguments (options and/or count)
     * @throws Exception if an I/O error occurs during file operations
     */
    @Override
    protected void executeWithException(List<String> args) throws Exception {
        int skipAmount = 0;

        for (int i = 0; i < args.size(); i++) {
            var arg = args.get(i);

            if (isInteger(arg)) {
                int n = Integer.parseInt(arg);
                if (n < 0) {
                    stdErr.printf((MSG_INVALID_NUMBER) + "%n", arg);
                    return;
                }
                skipAmount = Math.max(0, HISTORY.size() - n);
            } else if ("-r".equals(arg)) {
                if (i + 1 < args.size()) {
                    loadHistoryFromFile(args.get(++i));
                } else {
                    HISTORY.load();
                }
                historyWrittenCount = HISTORY.size();
                return;
            } else if ("-w".equals(arg)) {
                if (i + 1 < args.size()) {
                    writeHistoryToFile(args.get(++i));
                } else {
                    HISTORY.save();
                    historyWrittenCount = HISTORY.size();
                }
                return;
            } else if ("-a".equals(arg)) {
                if (i + 1 < args.size()) {
                    appendHistoryToFile(args.get(++i));
                } else {
                    HISTORY.save();
                    historyWrittenCount = HISTORY.size();
                }
                return;
            } else if ("-c".equals(arg)) {
                HISTORY.purge();
                historyWrittenCount = 0;
                return;
            } else if (arg.startsWith("-")) {
                stdErr.printf((MSG_INVALID_OPTION) + "%n", arg);
                return;
            }
        }

        displayHistory(skipAmount);
    }

    /**
     * Writes the entire in-memory history to the specified file, overwriting it.
     *
     * <p>
     * <b>Bash equivalent:</b> {@code history -w filename}
     * </p>
     *
     * <p>
     * <b>FILE_HISTORY_MAX_ENTRIES limit:</b> If the in-memory history exceeds this limit,
     * only the most recent entries are written (older entries are truncated).
     * This mirrors bash's {@code HISTFILESIZE} behavior.
     * </p>
     *
     * <p>
     * After writing, {@code historyWrittenCount} is updated so subsequent {@code -a}
     * calls won't duplicate these entries.
     * </p>
     *
     * @param filename path to the target file
     * @throws IOException if the file cannot be written
     */
    private void writeHistoryToFile(String filename) throws IOException {
        Path filePath = Path.of(filename);
        int skipAmount = Math.max(0, HISTORY.size() - FILE_HISTORY_MAX_ENTRIES);

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            int currentIndex = 0;
            for (var entry : HISTORY) {
                if (currentIndex >= skipAmount) {
                    writer.write(entry.line());
                    writer.newLine();
                }
                currentIndex++;
            }
        }

        historyWrittenCount = HISTORY.size();
        stdOut.printf((MSG_WRITTEN_TO_FILE) + "%n", filename);
    }

    /**
     * Appends only new history entries (since last write) to the specified file.
     *
     * <p>
     * <b>Bash equivalent:</b> {@code history -a filename}
     * </p>
     *
     * <p>
     * Any entry with index >= {@code historyWrittenCount} is considered new. This includes:
     * <ul>
     *   <li>Commands executed in the current session after the last write</li>
     *   <li>Entries loaded via {@code -r} (if historyWrittenCount wasn't updated - but we do update it)</li>
     * </ul>
     * </p>
     *
     * <p>
     * <b>Note:</b> Unlike {@code -w}, this does NOT apply FILE_HISTORY_MAX_ENTRIES limit.
     * The file may grow beyond the limit. Use {@code -w} periodically to truncate.
     * </p>
     *
     * @param filename path to the target file (created if doesn't exist)
     * @throws IOException if the file cannot be written
     */
    private void appendHistoryToFile(String filename) throws IOException {
        Path filePath = Path.of(filename);

        int newCommandsCount = HISTORY.size() - historyWrittenCount;

        if (newCommandsCount <= 0) {
            stdOut.println(MSG_NO_NEW_COMMANDS);
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                filePath,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {

            int currentIndex = 0;
            for (var entry : HISTORY) {
                if (currentIndex >= historyWrittenCount) {
                    writer.write(entry.line());
                    writer.newLine();
                }
                currentIndex++;
            }
        }

        historyWrittenCount = HISTORY.size();
        stdOut.printf((MSG_APPENDED_TO_FILE) + "%n", newCommandsCount, filename);
    }

    /**
     * Reads history entries from a file and appends them to the current in-memory history.
     *
     * <p>
     * <b>Bash equivalent:</b> {@code history -r filename}
     * </p>
     *
     * <p>
     * <b>Important behaviors:</b>
     * <ul>
     *   <li>Entries are APPENDED to existing history, not replaced</li>
     *   <li>Loading stops if IN_MEMORY_HISTORY_MAX_ENTRIES limit is reached</li>
     * </ul>
     * </p>
     *
     * <p>
     * <b>Note:</b> This method does not update {@code historyWrittenCount}.
     * The caller ({@code executeWithException}) is responsible for updating it
     * after this method returns.
     * </p>
     *
     * @param filename path to the source file
     * @throws IOException if the file cannot be read
     */
    private void loadHistoryFromFile(String filename) throws IOException {
        Path filePath = Path.of(filename);
        if (!Files.exists(filePath)) {
            stdOut.printf((MSG_FILE_NOT_FOUND) + "%n", filename);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int loadedCount = 0;
            int currentSize = HISTORY.size();

            while ((line = reader.readLine()) != null) {
                if (currentSize + loadedCount >= IN_MEMORY_HISTORY_MAX_ENTRIES) {
                    stdOut.println(MSG_MEMORY_LIMIT_REACHED);
                    break;
                }
                HISTORY.add(line);
                loadedCount++;
            }

            stdOut.printf((MSG_LOADED_FROM_FILE) + "%n", loadedCount, filename);
        }
    }

    /**
     * Displays history entries to stdout with line numbers.
     *
     * <p>
     * <b>Bash equivalent:</b> {@code history} or {@code history N}
     * </p>
     *
     * <p>
     * Output format matches bash: line numbers are right-aligned with padding,
     * followed by two spaces and the command text.
     * </p>
     *
     * <p>
     * Example output:
     * <pre>
     *     1  ls -la
     *     2  cd /home
     *     3  pwd
     * </pre>
     * </p>
     *
     * @param skipAmount number of oldest entries to skip (for "history N" behavior)
     */
    private void displayHistory(int skipAmount) {
        var iterator = HISTORY.iterator();

        for (int i = 0; i < skipAmount && iterator.hasNext(); i++) {
            iterator.next();
        }

        while (iterator.hasNext()) {
            var entry = iterator.next();
            stdOut.println("    " + (entry.index() + 1) + "  " + entry.line());
        }
    }
}
