package com.github.lowkkid.jsh.config;


import java.nio.file.Path;
import java.nio.file.Paths;

public final class EnvConfigReader {

    private EnvConfigReader() {}

    public static String HOME;
    public static String USERNAME;
    public static Boolean LOGS_ENABLED;
    public static Path UI_CONFIG_FILE;

    public static Path HISTORY_FILE;
    public static Integer IN_MEMORY_HISTORY_MAX_ENTRIES;
    public static Integer FILE_HISTORY_MAX_ENTRIES;
    public static Boolean INCREMENTAL_APPEND_HISTORY;



    static {
        HOME = System.getProperty("user.home");
        USERNAME = System.getProperty("user.name");

        LOGS_ENABLED = System.getenv("LOGS") != null && Boolean.parseBoolean(System.getenv("LOGS"));

        UI_CONFIG_FILE = System.getenv("JSH_UI_CONFIG") != null
                ? Paths.get(System.getenv("JSH_UI_CONFIG"))
                : Paths.get(HOME + "/.jshui");

        HISTORY_FILE = System.getenv("HISTFILE") != null
                ? Paths.get(System.getenv("HISTFILE"))
                : Paths.get(HOME + "/.jsh_history");
        IN_MEMORY_HISTORY_MAX_ENTRIES = System.getenv("HISTSIZE") != null
                ? Integer.parseInt(System.getenv("HISTSIZE"))
                : 1000;
        FILE_HISTORY_MAX_ENTRIES = System.getenv("HISTFILESIZE") != null
                ? Integer.parseInt(System.getenv("HISTFILESIZE"))
                : 2000;
        INCREMENTAL_APPEND_HISTORY = System.getenv("INC_APPEND_HISTORY") != null
                && Boolean.parseBoolean(System.getenv("INC_APPEND_HISTORY"));

    }

    private static Path getHistoryFilePath() {
        String historyPath = System.getenv("HISTFILE");

        if (historyPath != null && !historyPath.isEmpty()) {
            return Paths.get(historyPath);
        }

        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, ".jsh_history");
    }

}
