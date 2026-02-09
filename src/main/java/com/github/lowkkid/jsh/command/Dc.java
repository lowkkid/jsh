package com.github.lowkkid.jsh.command;

import com.github.lowkkid.jsh.Main;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

    private record ContainerInfo(String id, String name, String image, String status) {
    }

    @Override
    protected void executeWithException(List<String> args) throws Exception {
        Terminal terminal = Main.terminal;
        if (terminal == null) {
            stdErr.println("dc: terminal not available");
            return;
        }

        List<ContainerInfo> containers = fetchContainers();
        if (containers == null) {
            return;
        }
        if (containers.isEmpty()) {
            stdOut.println("No running containers.");
            return;
        }

        runTui(terminal, containers);
    }

    private List<ContainerInfo> fetchContainers() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "docker", "ps", "--format", "{{.ID}}\t{{.Names}}\t{{.Image}}\t{{.Status}}"
        );
        pb.redirectErrorStream(false);
        Process process = pb.start();

        List<ContainerInfo> containers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t", 4);
                if (parts.length == 4) {
                    containers.add(new ContainerInfo(parts[0], parts[1], parts[2], parts[3]));
                }
            }
        }

        String errorOutput = new String(
                process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8
        ).trim();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            stdErr.println("dc: " + (errorOutput.isEmpty() ? "docker command failed" : errorOutput));
            return null;
        }
        return containers;
    }

    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    private void runTui(Terminal terminal, List<ContainerInfo> containers)
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

    private void drawScreen(Terminal terminal, List<ContainerInfo> containers,
                            int selectedRow, int selectedCol) {
        StringBuilder sb = new StringBuilder();
        sb.append(CLEAR_SCREEN);

        sb.append("  ");
        sb.append(padRight("CONTAINER", COL_CONTAINER));
        sb.append(padRight("IMAGE", COL_IMAGE));
        sb.append(padRight("STATUS", COL_STATUS));
        sb.append("ACTIONS\n");

        for (int ii = 0; ii < containers.size(); ii++) {
            ContainerInfo ci = containers.get(ii);
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

    private void executeLogs(Terminal terminal, ContainerInfo container,
                             Attributes savedAttributes) throws IOException, InterruptedException {
        terminal.writer().print(CURSOR_SHOW + ALT_SCREEN_OFF);
        terminal.writer().flush();
        terminal.setAttributes(savedAttributes);

        ProcessBuilder pb = new ProcessBuilder("docker", "logs", "-f", container.name());
        pb.inheritIO();
        Process process = pb.start();

        Terminal.SignalHandler prevHandler = terminal.handle(
                Terminal.Signal.INT, sig -> process.destroy()
        );

        try {
            process.waitFor();
        } finally {
            terminal.handle(Terminal.Signal.INT, prevHandler);
        }

        Attributes newSaved = terminal.enterRawMode();
        terminal.writer().print(ALT_SCREEN_ON + CURSOR_HIDE);
        terminal.writer().flush();
    }

    private List<ContainerInfo> executeStop(Terminal terminal, List<ContainerInfo> containers,
                                            int selectedRow, Attributes savedAttributes)
            throws IOException, InterruptedException {
        ContainerInfo container = containers.get(selectedRow);

        terminal.writer().print(CLEAR_SCREEN + "Stopping " + container.name() + "...");
        terminal.writer().flush();

        ProcessBuilder pb = new ProcessBuilder("docker", "stop", container.name());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.getInputStream().readAllBytes();
        process.waitFor();

        List<ContainerInfo> refreshed = fetchContainers();
        if (refreshed == null) {
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
