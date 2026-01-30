package com.github.lowkkid.jsh.config;


import java.nio.file.Path;
import java.nio.file.Paths;

public final class EnvConfigReader {

    private EnvConfigReader() {}

    public static String HOME;
    public static String USERNAME;
    public static String HISTORY_FILE;
    public static Boolean LOGS_ENABLED;
    public static Path UI_CONFIG_FILE;


     static {
        HOME = System.getProperty("user.home");
        USERNAME = System.getProperty("user.name");
        HISTORY_FILE = System.getProperty("HISTFILE", HOME + ".jsh_history");
        LOGS_ENABLED = Boolean.parseBoolean(System.getProperty("LOGS", "false"));
        UI_CONFIG_FILE = Paths.get(System.getProperty("JSH_UI_CONFIG", HOME + "/.jshui"));
    }

}
