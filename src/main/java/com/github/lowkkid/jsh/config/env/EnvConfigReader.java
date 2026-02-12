package com.github.lowkkid.jsh.config.env;


import java.nio.file.Path;
import java.nio.file.Paths;

public final class EnvConfigReader {

    private EnvConfigReader() {
    }

    public static final String HOME = System.getProperty("user.home");
    public static String USERNAME = System.getProperty("user.name");
    public static Boolean LOGS_ENABLED;
    public static Path UI_CONFIG_FILE;

    public static Path HISTORY_FILE;
    public static Integer IN_MEMORY_HISTORY_MAX_ENTRIES;
    public static Integer FILE_HISTORY_MAX_ENTRIES;
    public static Boolean INCREMENTAL_APPEND_HISTORY;


    static {
        LOGS_ENABLED = EnvStorage.get("JSH_LOGS") != null && Boolean.parseBoolean(EnvStorage.get("JSH_LOGS"));

        UI_CONFIG_FILE = EnvStorage.get("JSH_UI_CONFIG") != null
                ? Paths.get(EnvStorage.get("JSH_UI_CONFIG"))
                : Paths.get(HOME + "/.jshui");

        HISTORY_FILE = EnvStorage.get("JSH_HISTFILE") != null
                ? Paths.get(EnvStorage.get("JSH_HISTFILE"))
                : Paths.get(HOME + "/.jsh_history");
        IN_MEMORY_HISTORY_MAX_ENTRIES = EnvStorage.get("JSH_HISTSIZE") != null
                ? Integer.parseInt(EnvStorage.get("JSH_HISTSIZE"))
                : 1000;
        FILE_HISTORY_MAX_ENTRIES = EnvStorage.get("JSH_HISTFILESIZE") != null
                ? Integer.parseInt(EnvStorage.get("JSH_HISTFILESIZE"))
                : 2000;
        INCREMENTAL_APPEND_HISTORY = EnvStorage.get("INC_APPEND_HISTORY") == null
                || Boolean.parseBoolean(EnvStorage.get("INC_APPEND_HISTORY"));

    }
}
