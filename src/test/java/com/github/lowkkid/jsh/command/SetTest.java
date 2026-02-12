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

class SetTest {

    private Set set;

    @BeforeEach
    void setUp() {
        set = new Set();
    }

    @AfterEach
    void tearDown() {
        EnvStorage.reset();
    }

    @Test
    void singleAssignment() {
        set.execute(List.of("foo=1"));

        assertEquals("1", EnvStorage.get("foo"));
        assertTrue(EnvStorage.getShellVars().containsKey("foo"));
        assertFalse(EnvStorage.getExportedVars().containsKey("foo"));
    }

    @Test
    void multipleAssignments() {
        set.execute(List.of("foo=1", "bar=2"));

        assertEquals("1", EnvStorage.get("foo"));
        assertEquals("2", EnvStorage.get("bar"));
    }

    @Test
    void overwritesShellVariable() {
        EnvStorage.putShell("foo", "1");

        set.execute(List.of("foo=2"));

        assertEquals("2", EnvStorage.get("foo"));
        assertTrue(EnvStorage.getShellVars().containsKey("foo"));
    }

    @Test
    void updatesExportedVariableInPlace() {
        EnvStorage.putExported("foo", "1");

        set.execute(List.of("foo=2"));

        assertEquals("2", EnvStorage.get("foo"));
        assertTrue(EnvStorage.getExportedVars().containsKey("foo"));
        assertFalse(EnvStorage.getShellVars().containsKey("foo"));
    }

    @Test
    void invalidArgumentIsSkippedValidStillProcessed() {
        set.execute(List.of("foo=1", "bar", "baz=3"));

        assertEquals("1", EnvStorage.get("foo"));
        assertNull(EnvStorage.get("bar"));
        assertEquals("3", EnvStorage.get("baz"));
    }

    @Test
    void assignmentWithEmptyValue() {
        set.execute(List.of("foo="));

        assertEquals("", EnvStorage.get("foo"));
    }

    @Test
    void equalsAtPositionZeroIgnored() {
        set.execute(List.of("=foo"));

        assertNull(EnvStorage.get("=foo"));
        assertNull(EnvStorage.get(""));
    }
}
