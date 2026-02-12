package com.github.lowkkid.jsh.config.env;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AliasStorageTest {

    @AfterEach
    void tearDown() {
        AliasStorage.reset();
    }

    @Test
    void putAndGet() {
        AliasStorage.put("ll", "ls -la");

        assertEquals("ls -la", AliasStorage.get("ll"));
    }

    @Test
    void getReturnsNullForUndefined() {
        assertNull(AliasStorage.get("nonexistent"));
    }

    @Test
    void putOverwritesExisting() {
        AliasStorage.put("ll", "ls -la");
        AliasStorage.put("ll", "ls -lah");

        assertEquals("ls -lah", AliasStorage.get("ll"));
    }

    @Test
    void removeExistingAlias() {
        AliasStorage.put("ll", "ls -la");

        assertTrue(AliasStorage.remove("ll"));
        assertNull(AliasStorage.get("ll"));
    }

    @Test
    void removeNonexistentReturnsFalse() {
        assertFalse(AliasStorage.remove("nonexistent"));
    }

    @Test
    void contains() {
        AliasStorage.put("ll", "ls -la");

        assertTrue(AliasStorage.contains("ll"));
        assertFalse(AliasStorage.contains("nonexistent"));
    }

    @Test
    void getAllReturnsAllAliases() {
        AliasStorage.put("ll", "ls -la");
        AliasStorage.put("gs", "git status");

        var all = AliasStorage.getAll();
        assertEquals(2, all.size());
        assertEquals("ls -la", all.get("ll"));
        assertEquals("git status", all.get("gs"));
    }

    @Test
    void reset() {
        AliasStorage.put("ll", "ls -la");
        AliasStorage.reset();

        assertTrue(AliasStorage.getAll().isEmpty());
    }
}
