package com.github.lowkkid.jsh.ui;

import com.github.lowkkid.jsh.config.PromptConfig;
import com.github.lowkkid.jsh.config.PromptConfigReader;
import com.github.lowkkid.jsh.config.StyleConfig;
import java.nio.file.Path;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;


/**
 * Builds the prompt string with powerline-style support.
 *
 * <p>The prompt consists of segments separated by separators:</p>
 * <pre>
 * [user][sep][path][sep][symbol]
 * </pre>
 * <h4>Powerline separators</h4>
 *
 * <p>For the separator to appear as a smooth transition between segments:</p>
 * <ul>
 *   <li>separator foreground = previous segment's background (the "arrow" color)</li>
 *   <li>separator background = next segment's background (fills the empty space)</li>
 * </ul>
 */
public class PromptBuilder {

    /**
     * Builds an ANSI prompt string for the specified directory.
     *
     * @param currentDir current working directory
     * @return string with ANSI escape sequences for colors
     */
    public String build(Path currentDir) {
        PromptConfig config = PromptConfigReader.getConfig();

        AttributedStringBuilder sb = new AttributedStringBuilder();

        StyleConfig userStyle = config.userStyle();
        StyleConfig pathStyle = config.pathStyle();
        StyleConfig promptSymbolStyle = config.promptSymbolStyle();

        // segment: username
        if (config.includeUser()) {
            sb.style(userStyle.toAttributedStyle())
                    .append(" ")
                    .append(System.getProperty("user.name"))
                    .append(" ");

            // separator after user: fg = user bg, bg = path bg
            String userSep = config.userSeparator().getSymbol();
            if (!userSep.isEmpty()) {
                AttributedStyle sepStyle = buildSeparatorStyle(
                        userStyle.getBackgroundColor(),
                        pathStyle.getBackgroundColor()
                );
                sb.style(sepStyle).append(userSep);
            }
        }

        // segment: current path
        String path = formatPath(currentDir);
        sb.style(pathStyle.toAttributedStyle())
                .append(" ")
                .append(path)
                .append(" ");

        // separator after path
        String pathSep = config.pathSeparator().getSymbol();
        if (!pathSep.isEmpty()) {
            AttributedStyle sepStyle = buildSeparatorStyle(
                    pathStyle.getBackgroundColor(),
                    promptSymbolStyle.getBackgroundColor()
            );
            sb.style(sepStyle).append(pathSep);
        }

        // segment: prompt symbol
        if (config.promptSymbol() != null && !config.promptSymbol().isBlank()) {
            sb.style(promptSymbolStyle.toAttributedStyle())
                    .append(" ")
                    .append(config.promptSymbol().trim());
        }

        // space with no styles between prompt and user input
        sb.style(AttributedStyle.DEFAULT).append(" ");

        return sb.toAnsi();
    }

    /**
     * Builds a style for a powerline separator.
     *
     * @param foregroundColor the "arrow" color (previous segment's background)
     * @param backgroundColor the fill color (next segment's background), -1 = transparent
     * @return style for the separator
     */
    private AttributedStyle buildSeparatorStyle(int foregroundColor, int backgroundColor) {
        AttributedStyle style = AttributedStyle.DEFAULT;

        if (foregroundColor >= 0) {
            style = style.foreground(foregroundColor);
        }

        if (backgroundColor >= 0) {
            style = style.background(backgroundColor);
        }

        return style;
    }

    /**
     * Formats the path, replacing the home directory with "~".
     *
     * @param currentDir full path
     * @return path with "~" instead of /home/user
     */
    private String formatPath(Path currentDir) {
        String path = currentDir.toString();
        String home = System.getProperty("user.home");

        if (path.startsWith(home)) {
            path = "~" + path.substring(home.length());
        }

        return path;
    }
}
