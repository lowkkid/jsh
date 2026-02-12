package com.github.lowkkid.jsh.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.lowkkid.jsh.config.env.AliasStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AliasExpansionTest {

    private final InputParser parser = new InputParser();

    @AfterEach
    void tearDown() {
        AliasStorage.reset();
    }

    @Test
    void aliasExpandsToCommand() {
        AliasStorage.put("ll", "ls -la");

        var result = parser.getCommandAndArgs("ll");

        assertEquals(1, result.size());
        assertEquals("ls", result.getFirst().command());
        assertEquals(1, result.getFirst().arguments().size());
        assertEquals("-la", result.getFirst().arguments().getFirst());
    }

    @Test
    void aliasWithAdditionalArgs() {
        AliasStorage.put("ll", "ls -la");

        var result = parser.getCommandAndArgs("ll /tmp");

        assertEquals(1, result.size());
        assertEquals("ls", result.getFirst().command());
        assertEquals(2, result.getFirst().arguments().size());
        assertEquals("-la", result.getFirst().arguments().get(0));
        assertEquals("/tmp", result.getFirst().arguments().get(1));
    }

    @Test
    void aliasWithPipe() {
        AliasStorage.put("greplogs", "cat /tmp/test | grep foo");

        var result = parser.getCommandAndArgs("greplogs");

        assertEquals(2, result.size());
        assertEquals("cat", result.get(0).command());
        assertEquals("grep", result.get(1).command());
    }

    @Test
    void noExpansionForNonAlias() {
        var result = parser.getCommandAndArgs("echo hello");

        assertEquals(1, result.size());
        assertEquals("echo", result.getFirst().command());
        assertEquals("hello", result.getFirst().arguments().getFirst());
    }

    @Test
    void recursiveAliasDoesNotLoop() {
        AliasStorage.put("ls", "ls --color=auto");

        var result = parser.getCommandAndArgs("ls");

        assertEquals(1, result.size());
        assertEquals("ls", result.getFirst().command());
        assertEquals("--color=auto", result.getFirst().arguments().getFirst());
    }

    @Test
    void chainedAliasExpansion() {
        AliasStorage.put("ll", "myls -la");
        AliasStorage.put("myls", "ls --color");

        var result = parser.getCommandAndArgs("ll");

        assertEquals(1, result.size());
        assertEquals("ls", result.getFirst().command());
    }
}
