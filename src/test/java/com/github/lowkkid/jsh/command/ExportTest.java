package com.github.lowkkid.jsh.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.lowkkid.jsh.config.env.EnvStorage;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExportTest {

    private Export export;

    @BeforeEach
    void setUp() {
        export = new Export();
    }

    @AfterEach
    void tearDown() {
        EnvStorage.reset();
    }

    @Test
    void exportWithValue() {
        export.execute(List.of("foo=1"));

        assertEquals("1", EnvStorage.get("foo"));
        assertTrue(EnvStorage.getExportedVars().containsKey("foo"));
        assertFalse(EnvStorage.getShellVars().containsKey("foo"));
    }

    @Test
    void exportOverwritesExistingExported() {
        EnvStorage.putExported("foo", "1");

        export.execute(List.of("foo=2"));

        assertEquals("2", EnvStorage.get("foo"));
        assertTrue(EnvStorage.getExportedVars().containsKey("foo"));
    }

    @Test
    void exportPromotesShellVariable() {
        EnvStorage.putShell("foo", "1");

        export.execute(List.of("foo"));

        assertEquals("1", EnvStorage.get("foo"));
        assertTrue(EnvStorage.getExportedVars().containsKey("foo"));
        assertFalse(EnvStorage.getShellVars().containsKey("foo"));
    }

    @Test
    void exportPromotesShellVariableWithNewValue() {
        EnvStorage.putShell("foo", "1");

        export.execute(List.of("foo=2"));

        assertEquals("2", EnvStorage.get("foo"));
        assertTrue(EnvStorage.getExportedVars().containsKey("foo"));
        assertFalse(EnvStorage.getShellVars().containsKey("foo"));
    }

    @Test
    void exportNonexistentVariableCreatesEmpty() {
        export.execute(List.of("foo"));

        assertEquals("", EnvStorage.get("foo"));
        assertTrue(EnvStorage.getExportedVars().containsKey("foo"));
    }

    @Test
    void exportAlreadyExportedWithoutValueIsNoop() {
        EnvStorage.putExported("foo", "1");

        export.execute(List.of("foo"));

        assertEquals("1", EnvStorage.get("foo"));
    }

    @Test
    void exportMultipleArguments() {
        EnvStorage.putShell("bar", "existing");

        export.execute(List.of("foo=1", "bar", "baz=3"));

        assertEquals("1", EnvStorage.get("foo"));
        assertEquals("existing", EnvStorage.get("bar"));
        assertEquals("3", EnvStorage.get("baz"));
        assertTrue(EnvStorage.getExportedVars().containsKey("foo"));
        assertTrue(EnvStorage.getExportedVars().containsKey("bar"));
        assertTrue(EnvStorage.getExportedVars().containsKey("baz"));
    }

    @Test
    void exportWithEmptyValue() {
        export.execute(List.of("foo="));

        assertEquals("", EnvStorage.get("foo"));
        assertTrue(EnvStorage.getExportedVars().containsKey("foo"));
    }

    @Test
    void exportIgnoresInvalidArgument() {
        export.execute(List.of("=invalid"));

        assertNull(EnvStorage.get("=invalid"));
    }
}
