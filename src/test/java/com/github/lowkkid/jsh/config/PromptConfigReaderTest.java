package com.github.lowkkid.jsh.config;

import static com.github.lowkkid.jsh.config.env.EnvConfigReader.UI_CONFIG_FILE;
import static com.github.lowkkid.jsh.config.SeparatorStyle.TRIANGLE;
import static com.github.lowkkid.jsh.data.MockData.MOCK_PROMPT_CONFIG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PromptConfigReaderTest {

    @TempDir
    Path tempDir;

    private Path originalUIConfigFile;

    @BeforeEach
    void setUp() {
        originalUIConfigFile = UI_CONFIG_FILE;
    }

    @AfterEach
    void tearDown() {
        UI_CONFIG_FILE = originalUIConfigFile;
        PromptConfigReader.reset();
    }

    @Nested
    @DisplayName("Initial reads")
    class InitialReads {
        @Test
        void readValidConfig() throws IOException {
            UI_CONFIG_FILE = writeMockConfig();

            var config = PromptConfigReader.getConfig();

            assertEqualityToMockConfig(config);
        }

        @Test
        void returnDefaultConfigWhenConfigFileNotExists() {
            UI_CONFIG_FILE = Paths.get("non-existent-path");

            var config = PromptConfigReader.getConfig();

            assertEquals(PromptConfig.DEFAULT(), config);
        }
    }

    @Nested
    @DisplayName("Repeatable reads")
    class RepeatableReads {

        @Test
        void readPreparedConfig() throws IOException {
            UI_CONFIG_FILE = writeMockConfig();
            PromptConfigReader.getConfig();

            var config = PromptConfigReader.getConfig();

            assertEqualityToMockConfig(config);
        }
    }

    private Path writeMockConfig() throws IOException {
        Path configFile = tempDir.resolve(".jshui");
        Files.write(configFile, MOCK_PROMPT_CONFIG.getBytes());
        return configFile;
    }

    private void assertEqualityToMockConfig(PromptConfig config) {
        assertNotNull(config);
        assertTrue(config.includeUser());
        assertNotNull(config.userStyle());
        assertEquals(TRIANGLE, config.userSeparator());
        assertNotNull(config.pathStyle());
        assertEquals(TRIANGLE, config.pathSeparator());
        assertEquals("$", config.promptSymbol());
    }
}
