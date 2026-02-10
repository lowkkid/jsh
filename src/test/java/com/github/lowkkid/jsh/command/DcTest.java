package com.github.lowkkid.jsh.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.github.lowkkid.jsh.Main;
import com.github.lowkkid.jsh.command.utils.DockerClient;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DcTest extends CommandTestBase {

    @Mock
    private DockerClient mockDockerClient;

    @Mock
    private Terminal mockTerminal;

    private Dc dc;
    private Terminal originalTerminal;

    @BeforeEach
    void setUp() {
        dc = new Dc(mockDockerClient);
        injectStreams(dc);
        originalTerminal = Main.terminal;
    }

    @AfterEach
    void tearDown() {
        Main.terminal = originalTerminal;
    }

    @Nested
    class TerminalNotAvailable {

        @Test
        void printsErrorWhenTerminalIsNull() {
            Main.terminal = null;

            dc.execute(Collections.emptyList());

            assertEquals("dc: terminal not available", getStdErrTrimmed());
            assertTrue(getStdOut().isEmpty());
            verifyNoInteractions(mockDockerClient);
        }
    }

    @Nested
    class NoRunningContainers {

        @Test
        void printsMessageWhenNoContainers() throws Exception {
            Main.terminal = mockTerminal;
            when(mockDockerClient.fetchContainers()).thenReturn(Collections.emptyList());

            dc.execute(Collections.emptyList());

            assertEquals("No running containers.", getStdOutTrimmed());
            assertTrue(getStdErr().isEmpty());
        }
    }

    @Nested
    class DockerError {

        @Test
        void printsErrorWhenDockerFails() throws Exception {
            Main.terminal = mockTerminal;
            when(mockDockerClient.fetchContainers())
                    .thenThrow(new IOException("Cannot connect to the Docker daemon"));

            dc.execute(Collections.emptyList());

            assertEquals("dc: Cannot connect to the Docker daemon", getStdErrTrimmed());
            assertTrue(getStdOut().isEmpty());
        }

        @Test
        void printsGenericErrorOnUnexpectedFailure() throws Exception {
            Main.terminal = mockTerminal;
            when(mockDockerClient.fetchContainers())
                    .thenThrow(new IOException("docker command failed"));

            dc.execute(Collections.emptyList());

            assertEquals("dc: docker command failed", getStdErrTrimmed());
            assertTrue(getStdOut().isEmpty());
        }
    }

    @Nested
    class ContainersFound {

        @Test
        void doesNotPrintNoContainersMessage() throws Exception {
            Main.terminal = mockTerminal;
            List<DockerClient.ContainerInfo> containers = List.of(
                    new DockerClient.ContainerInfo("abc123", "web", "nginx:latest", "Up 2 hours")
            );
            when(mockDockerClient.fetchContainers()).thenReturn(containers);

            // runTui will throw NPE because mockTerminal.enterRawMode() returns null,
            // but execute() catches all exceptions via Command.execute()
            dc.execute(Collections.emptyList());

            // Verify we did NOT get "No running containers." or terminal error
            String stdOut = getStdOut();
            String stdErr = getStdErr();
            assertFalse(stdOut.contains("No running containers."));
            assertFalse(stdErr.contains("dc: terminal not available"));
        }
    }
}
