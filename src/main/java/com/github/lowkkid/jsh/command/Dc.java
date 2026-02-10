package com.github.lowkkid.jsh.command;

import com.github.lowkkid.jsh.Main;
import com.github.lowkkid.jsh.command.utils.DefaultDockerClient;
import com.github.lowkkid.jsh.command.utils.DockerClient;
import java.io.IOException;
import java.util.List;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;

public class Dc extends Command {

    private static final int ESC = 27;
    private static final int PEEK_TIMEOUT = 50;

    private static final String ALT_SCREEN_ON = "\033[?1049h";
    private static final String ALT_SCREEN_OFF = "\033[?1049l";
    private static final String CURSOR_HIDE = "\033[?25l";
    private static final String CURSOR_SHOW = "\033[?25h";
    private static final String CLEAR_SCREEN = "\033[2J\033[H";
    private static final String REVERSE_ON = "\033[7m";
    private static final String REVERSE_OFF = "\033[m";

    private static final int COL_CONTAINER = 20;
    private static final int COL_IMAGE = 20;
    private static final int COL_STATUS = 22;

    private final DockerClient dockerClient;

    public Dc() {
        this(new DefaultDockerClient());
    }

    public Dc(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    @Override
    protected void executeWithException(List<String> args) throws Exception {
        Terminal terminal = Main.terminal;
        if (terminal == null) {
            stdErr.println("dc: terminal not available");
            return;
        }

        List<DockerClient.ContainerInfo> containers;
        try {
            containers = dockerClient.fetchContainers();
        } catch (IOException ex) {
            stdErr.println("dc: " + ex.getMessage());
            return;
        }
        if (containers.isEmpty()) {
            stdOut.println("No running containers.");
            return;
        }

        runTui(terminal, containers);
    }

    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    private void runTui(Terminal terminal, List<DockerClient.ContainerInfo> containers)
            throws IOException, InterruptedException {
        int selectedRow = 0;
        int selectedCol = 0;
        Attributes savedAttributes = terminal.enterRawMode();

        terminal.writer().print(ALT_SCREEN_ON + CURSOR_HIDE);
        terminal.writer().flush();

        try {
            drawScreen(terminal, containers, selectedRow, selectedCol);

            while (true) {
                int ch = terminal.reader().read();
                if (ch == -1 || ch == 'q') {
                    break;
                }

                if (ch == ESC) {
                    int next = terminal.reader().peek(PEEK_TIMEOUT);
                    if (next == -1) {
                        break;
                    }
                    if (next == '[') {
                        terminal.reader().read();
                        int arrow = terminal.reader().read();
                        switch (arrow) {
                            case 'A' -> selectedRow = Math.max(0, selectedRow - 1);
                            case 'B' -> selectedRow = Math.min(containers.size() - 1, selectedRow + 1);
                            case 'C' -> selectedCol = Math.min(1, selectedCol + 1);
                            case 'D' -> selectedCol = Math.max(0, selectedCol - 1);
                            default -> { }
                        }
                    }
                } else if (ch == '\r' || ch == '\n') {
                    if (selectedCol == 0) {
                        executeLogs(terminal, containers.get(selectedRow), savedAttributes);
                    } else {
                        containers = executeStop(
                                terminal, containers, selectedRow, savedAttributes
                        );
                        if (containers == null || containers.isEmpty()) {
                            break;
                        }
                        if (selectedRow >= containers.size()) {
                            selectedRow = containers.size() - 1;
                        }
                    }
                }

                drawScreen(terminal, containers, selectedRow, selectedCol);
            }
        } finally {
            terminal.writer().print(CURSOR_SHOW + ALT_SCREEN_OFF);
            terminal.writer().flush();
            terminal.setAttributes(savedAttributes);
        }
    }

    private void drawScreen(Terminal terminal, List<DockerClient.ContainerInfo> containers,
                            int selectedRow, int selectedCol) {
        StringBuilder sb = new StringBuilder();
        sb.append(CLEAR_SCREEN);

        sb.append("  ");
        sb.append(padRight("CONTAINER", COL_CONTAINER));
        sb.append(padRight("IMAGE", COL_IMAGE));
        sb.append(padRight("STATUS", COL_STATUS));
        sb.append("ACTIONS\n");

        for (int ii = 0; ii < containers.size(); ii++) {
            DockerClient.ContainerInfo ci = containers.get(ii);
            boolean isSelected = ii == selectedRow;

            sb.append(isSelected ? "> " : "  ");
            sb.append(padRight(ci.name(), COL_CONTAINER));
            sb.append(padRight(ci.image(), COL_IMAGE));
            sb.append(padRight(ci.status(), COL_STATUS));

            if (isSelected && selectedCol == 0) {
                sb.append(REVERSE_ON).append("[logs]").append(REVERSE_OFF);
            } else {
                sb.append("[logs]");
            }
            sb.append("  ");
            if (isSelected && selectedCol == 1) {
                sb.append(REVERSE_ON).append("[stop]").append(REVERSE_OFF);
            } else {
                sb.append("[stop]");
            }
            sb.append('\n');
        }

        sb.append("\n  Up/Down navigate  Left/Right select action  Enter execute  q/Esc quit");

        terminal.writer().print(sb);
        terminal.writer().flush();
    }

    private void executeLogs(Terminal terminal, DockerClient.ContainerInfo container,
                             Attributes savedAttributes) throws IOException, InterruptedException {
        terminal.writer().print(CURSOR_SHOW + ALT_SCREEN_OFF);
        terminal.writer().flush();
        terminal.setAttributes(savedAttributes);

        Process process = dockerClient.showLogs(container.name());

        Terminal.SignalHandler prevHandler = terminal.handle(
                Terminal.Signal.INT, sig -> process.destroy()
        );

        try {
            process.waitFor();
        } finally {
            terminal.handle(Terminal.Signal.INT, prevHandler);
        }

        terminal.enterRawMode();
        terminal.writer().print(ALT_SCREEN_ON + CURSOR_HIDE);
        terminal.writer().flush();
    }

    private List<DockerClient.ContainerInfo> executeStop(
            Terminal terminal, List<DockerClient.ContainerInfo> containers,
            int selectedRow, Attributes savedAttributes)
            throws IOException, InterruptedException {
        DockerClient.ContainerInfo container = containers.get(selectedRow);

        terminal.writer().print(CLEAR_SCREEN + "Stopping " + container.name() + "...");
        terminal.writer().flush();

        dockerClient.stopContainer(container.name());

        List<DockerClient.ContainerInfo> refreshed;
        try {
            refreshed = dockerClient.fetchContainers();
        } catch (IOException ex) {
            stdErr.println("dc: " + ex.getMessage());
            return null;
        }
        if (refreshed.isEmpty()) {
            terminal.writer().print(CURSOR_SHOW + ALT_SCREEN_OFF);
            terminal.writer().flush();
            terminal.setAttributes(savedAttributes);
            stdOut.println("No running containers.");
            return refreshed;
        }
        return refreshed;
    }

    private static String padRight(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width - 1) + " ";
        }
        return text + " ".repeat(width - text.length());
    }
}
