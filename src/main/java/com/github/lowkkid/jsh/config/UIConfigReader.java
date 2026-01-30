package com.github.lowkkid.jsh.config;

import static com.github.lowkkid.jsh.config.EnvConfigReader.UI_CONFIG_FILE;
import static com.github.lowkkid.jsh.logger.LogMessages.UI_CONFIG_FILE_DOES_NOT_EXIST;
import static com.github.lowkkid.jsh.logger.LogMessages.UI_CONFIG_READ_ERROR;

import com.github.lowkkid.jsh.logger.Logger;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class UIConfigReader {

    private static PromptConfig readPromptConfig(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return new GsonBuilder()
                    .create()
                    .fromJson(reader, PromptConfig.class);
        }
    }

    public static PromptConfig readPromptConfigOrDefault() {
        var configFile = UI_CONFIG_FILE;
        try {
            if (Files.exists(configFile)) {
                return readPromptConfig(configFile);
            } else {
                Logger.log(String.format(UI_CONFIG_FILE_DOES_NOT_EXIST, configFile));
            }
        } catch (IOException ignored) {
            Logger.log(String.format(UI_CONFIG_READ_ERROR, configFile));
        }
        return PromptConfig.DEFAULT();
    }
}
