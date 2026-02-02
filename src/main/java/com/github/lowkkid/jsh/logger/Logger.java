package com.github.lowkkid.jsh.logger;

import static com.github.lowkkid.jsh.config.EnvConfigReader.LOGS_ENABLED;

public final class Logger {

    private Logger() {
    }

    public static void log(String message) {
        if (LOGS_ENABLED) {
            System.out.println(message);
        }
    }
}
