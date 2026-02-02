package com.github.lowkkid.jsh.command;

import com.github.lowkkid.jsh.command.utils.CommandRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TypeTest extends CommandTestBase {

    @Mock
    private CommandRegistry mockRegistry;

    @Mock
    private Function<String, String> mockPathLookup;

    private Type type;

    @BeforeEach
    void setUp() {
        type = new Type(mockRegistry, mockPathLookup);
        injectStreams(type);
    }

    @Nested
    class BuiltInCommands {

        @Test
        void recognizesBuiltInCommand() {
            when(mockRegistry.isBultInCommand("echo")).thenReturn(true);

            type.execute(List.of("echo"));

            assertEquals("echo is a shell builtin", getStdOutTrimmed());
            assertTrue(getStdErr().isEmpty());
            verify(mockRegistry).isBultInCommand("echo");
            verifyNoInteractions(mockPathLookup);
        }

        @Test
        void recognizesMultipleBuiltInCommands() {
            when(mockRegistry.isBultInCommand("cd")).thenReturn(true);
            when(mockRegistry.isBultInCommand("pwd")).thenReturn(true);

            type.execute(List.of("cd", "pwd"));

            String output = getStdOut();
            assertTrue(output.contains("cd is a shell builtin"));
            assertTrue(output.contains("pwd is a shell builtin"));
            verifyNoInteractions(mockPathLookup);
        }
    }

    @Nested
    class ExternalCommands {

        @Test
        void recognizesExternalCommand() {
            when(mockRegistry.isBultInCommand("ls")).thenReturn(false);
            when(mockPathLookup.apply("ls")).thenReturn("/usr/bin/ls");

            type.execute(List.of("ls"));

            assertEquals("ls is /usr/bin/ls", getStdOutTrimmed());
            verify(mockPathLookup).apply("ls");
        }

        @Test
        void recognizesExternalCommandInDifferentPath() {
            when(mockRegistry.isBultInCommand("git")).thenReturn(false);
            when(mockPathLookup.apply("git")).thenReturn("/opt/git/bin/git");

            type.execute(List.of("git"));

            assertEquals("git is /opt/git/bin/git", getStdOutTrimmed());
        }
    }

    @Nested
    class NotFoundCommands {

        @Test
        void reportsNotFoundWhenPathLookupReturnsNull() {
            when(mockRegistry.isBultInCommand("nonexistent")).thenReturn(false);
            when(mockPathLookup.apply("nonexistent")).thenReturn(null);

            type.execute(List.of("nonexistent"));

            assertEquals("nonexistent: not found", getStdOutTrimmed());
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void emptyArgumentsProducesNoOutput() {
            type.execute(List.of());

            assertTrue(getStdOut().isEmpty());
            assertTrue(getStdErr().isEmpty());
            verifyNoInteractions(mockRegistry);
            verifyNoInteractions(mockPathLookup);
        }
    }
}
