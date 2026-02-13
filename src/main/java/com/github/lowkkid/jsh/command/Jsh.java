package com.github.lowkkid.jsh.command;

import com.github.lowkkid.jsh.Main;
import com.github.lowkkid.jsh.command.utils.CommandRegistry;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.List;
import java.util.Properties;

public class Jsh extends Command {

    private static final String RESET = "\033[0m";
    private static final String BOLD = "\033[1m";
    private static final String CYAN = "\033[36m";
    private static final String WHITE = "\033[37m";
    private static final String BOLD_CYAN = BOLD + CYAN;

    // CHECKSTYLE.OFF: LineLength
    private static final String[] LOGO = {
        "\n",
        "\033[36m░░░░░██╗░██████╗██╗░░██╗\033[0m",
        "\033[36m░░░░░██║██╔════╝██║░░██║\033[0m",
        "\033[36m░░░░░██║╚█████╗░███████║\033[0m",
        "\033[36m██╗░░██║░╚═══██╗██╔══██║\033[0m",
        "\033[36m╚█████╔╝██████╔╝██║░░██║\033[0m",
        "\033[36m░╚════╝░╚═════╝░╚═╝░░╚═╝\033[0m",
    };
    // CHECKSTYLE.ON: LineLength

    private static final int LOGO_WIDTH = 28;
    private static final String PADDING = " ".repeat(LOGO_WIDTH);

    @Override
    public void executeWithException(List<String> args) throws Exception {
        String version = loadVersion();
        String[] infoLines = buildInfoLines(version);

        stdOut.println();
        int maxLines = Math.max(LOGO.length, infoLines.length);
        for (int i = 0; i < maxLines; i++) {
            String logoLine = i < LOGO.length ? LOGO[i] : "";
            String infoPart = i < infoLines.length ? infoLines[i] : "";
            String logoPadded = i < LOGO.length ? logoLine + spacing(logoLine) : PADDING;
            stdOut.println(logoPadded + "  " + infoPart);
        }
        stdOut.println();
    }

    private String[] buildInfoLines(String version) {
        String user = System.getProperty("user.name");
        String hostname = getHostname();
        String title = " " + BOLD_CYAN + user + WHITE + "@" + BOLD_CYAN + hostname + RESET;
        String separator = CYAN + "─".repeat(user.length() + 1 + hostname.length()) + RESET;

        String os = System.getProperty("os.name") + " " + System.getProperty("os.version");
        String kernel = System.getProperty("os.arch");
        String java = System.getProperty("java.version");
        String terminal = System.getenv("TERM") != null ? System.getenv("TERM") : "unknown";
        String workingDir = Main.currentDir.toString();
        int builtIns = CommandRegistry.getInstance().getBuiltInCommandCount();
        String colorPalette = buildColorPalette();

        return new String[] {
            title,
            separator,
            infoLine("Shell", "jsh " + version),
            infoLine("Java", java),
            infoLine("OS", os),
            infoLine("Kernel", kernel),
            infoLine("Terminal", terminal),
            infoLine("Working Dir", workingDir),
            infoLine("Built-ins", String.valueOf(builtIns)),
            "",
            colorPalette,
        };
    }

    private String infoLine(String label, String value) {
        return BOLD_CYAN + label + RESET + ": " + value;
    }

    static String loadVersion() {
        Properties props = new Properties();
        try (InputStream in = Jsh.class.getResourceAsStream("/version.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ignored) {
            // fall through
        }
        return props.getProperty("jsh.version", "unknown");
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String spacing(String logoLine) {
        int visibleLen = stripAnsi(logoLine).length();
        int pad = LOGO_WIDTH - visibleLen;
        return pad > 0 ? " ".repeat(pad) : "";
    }

    private String stripAnsi(String text) {
        return text.replaceAll("\033\\[[;\\d]*m", "");
    }

    private String buildColorPalette() {
        var sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append("\033[4").append(i).append("m   ");
        }
        sb.append(RESET);
        return sb.toString();
    }
}
