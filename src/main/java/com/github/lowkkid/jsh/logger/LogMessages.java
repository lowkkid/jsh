package com.github.lowkkid.jsh.logger;

public final class LogMessages {

    private LogMessages() {
    }

    // config parsing errors
    public static String UNKNOWN_COLOR_IN_CONFIG = "WARNING: unknown color in config: %s, transparent applied";
    public static String UI_CONFIG_FILE_DOES_NOT_EXIST =
            "ERROR: UI config file does not exist: %s, default config applied";
    public static String UI_CONFIG_READ_ERROR =
            "ERROR: Error reading UI config from: %s, default config applied";
}
