package com.github.lowkkid.jsh.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.lowkkid.jsh.config.env.AliasStorage;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AliasTest extends CommandTestBase {

    private Alias alias;

    @BeforeEach
    void setUp() {
        alias = new Alias();
        injectStreams(alias);
    }

    @AfterEach
    void cleanUp() {
        AliasStorage.reset();
    }

    @Test
    void noArgsWithNoAliases() {
        alias.execute(List.of());

        assertEquals("", getStdOut());
    }

    @Test
    void noArgsPrintsAllAliases() {
        AliasStorage.put("ll", "ls -la");
        AliasStorage.put("gs", "git status");

        alias.execute(List.of());

        String output = getStdOutTrimmed();
        assertTrue(output.contains("alias gs='git status'"));
        assertTrue(output.contains("alias ll='ls -la'"));
    }

    @Test
    void setAlias() {
        alias.execute(List.of("ll=ls -la"));

        assertEquals("ls -la", AliasStorage.get("ll"));
        assertTrue(getStdOut().isEmpty());
    }

    @Test
    void setAliasWithPipe() {
        alias.execute(List.of("greplogs=cat /tmp/test | grep foo"));

        assertEquals("cat /tmp/test | grep foo", AliasStorage.get("greplogs"));
    }

    @Test
    void showSpecificAlias() {
        AliasStorage.put("ll", "ls -la");

        alias.execute(List.of("ll"));

        assertEquals("alias ll='ls -la'", getStdOutTrimmed());
    }

    @Test
    void showNonexistentAlias() {
        alias.execute(List.of("nonexistent"));

        assertEquals("alias: nonexistent: not found", getStdErrTrimmed());
    }

    @Test
    void aliasesSortedAlphabetically() {
        AliasStorage.put("z", "zzz");
        AliasStorage.put("a", "aaa");
        AliasStorage.put("m", "mmm");

        alias.execute(List.of());

        String output = getStdOutTrimmed();
        String[] lines = output.split(System.lineSeparator());
        assertEquals(3, lines.length);
        assertTrue(lines[0].contains("alias a="));
        assertTrue(lines[1].contains("alias m="));
        assertTrue(lines[2].contains("alias z="));
    }

    @Test
    void overwriteExistingAlias() {
        AliasStorage.put("ll", "ls -la");

        alias.execute(List.of("ll=ls -lah"));

        assertEquals("ls -lah", AliasStorage.get("ll"));
    }
}
