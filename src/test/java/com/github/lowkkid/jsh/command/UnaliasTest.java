package com.github.lowkkid.jsh.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.lowkkid.jsh.config.env.AliasStorage;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UnaliasTest extends CommandTestBase {

    private Unalias unalias;

    @BeforeEach
    void setUp() {
        unalias = new Unalias();
        injectStreams(unalias);
    }

    @AfterEach
    void cleanUp() {
        AliasStorage.reset();
    }

    @Test
    void removeExistingAlias() {
        AliasStorage.put("ll", "ls -la");

        unalias.execute(List.of("ll"));

        assertFalse(AliasStorage.contains("ll"));
        assertTrue(getStdErr().isEmpty());
    }

    @Test
    void removeNonexistentAlias() {
        unalias.execute(List.of("nonexistent"));

        assertEquals("unalias: nonexistent: not found", getStdErrTrimmed());
    }

    @Test
    void removeMultipleAliases() {
        AliasStorage.put("ll", "ls -la");
        AliasStorage.put("gs", "git status");

        unalias.execute(List.of("ll", "gs"));

        assertFalse(AliasStorage.contains("ll"));
        assertFalse(AliasStorage.contains("gs"));
        assertTrue(getStdErr().isEmpty());
    }

    @Test
    void noArgsShowsUsage() {
        unalias.execute(List.of());

        assertEquals("unalias: usage: unalias name [name ...]", getStdErrTrimmed());
    }

    @Test
    void mixedExistingAndNonexistent() {
        AliasStorage.put("ll", "ls -la");

        unalias.execute(List.of("ll", "nonexistent"));

        assertFalse(AliasStorage.contains("ll"));
        assertEquals("unalias: nonexistent: not found", getStdErrTrimmed());
    }
}
