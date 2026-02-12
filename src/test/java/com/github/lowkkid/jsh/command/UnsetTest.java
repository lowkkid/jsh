package com.github.lowkkid.jsh.command;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.github.lowkkid.jsh.config.env.EnvStorage;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UnsetTest {

    private Unset unset;

    @BeforeEach
    void setUp() {
        unset = new Unset();
    }

    @AfterEach
    void tearDown() {
        EnvStorage.reset();
    }

    @Test
    void unsetShellVariable() {
        EnvStorage.putShell("foo", "1");

        unset.execute(List.of("foo"));

        assertNull(EnvStorage.get("foo"));
        assertFalse(EnvStorage.getShellVars().containsKey("foo"));
    }

    @Test
    void unsetExportedVariable() {
        EnvStorage.putExported("foo", "1");

        unset.execute(List.of("foo"));

        assertNull(EnvStorage.get("foo"));
        assertFalse(EnvStorage.getExportedVars().containsKey("foo"));
    }

    @Test
    void unsetNonexistentVariableIsNoop() {
        unset.execute(List.of("nonexistent"));

        assertNull(EnvStorage.get("nonexistent"));
    }

    @Test
    void unsetMultipleVariables() {
        EnvStorage.putShell("foo", "1");
        EnvStorage.putExported("bar", "2");

        unset.execute(List.of("foo", "bar"));

        assertNull(EnvStorage.get("foo"));
        assertNull(EnvStorage.get("bar"));
    }

    @Test
    void unsetWithNoArgs() {
        EnvStorage.putShell("foo", "1");

        unset.execute(List.of());

        // nothing happens, foo is still there
        assertFalse(EnvStorage.getShellVars().isEmpty());
    }
}
