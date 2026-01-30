package com.github.lowkkid.jsh.config;

import com.github.lowkkid.jsh.ui.PromptBuilder;
import org.jline.utils.AttributedStyle;

/**
 * Text style configuration for a prompt segment.
 *
 *
 * <p>Example in JSON config:</p>
 * <pre>
 * {
 *   "foreground": "white",
 *   "background": "blue",
 *   "bold": true,
 *   "italic": false
 * }
 * </pre>
 *
 * <p>Available colors: black, red, green, yellow, blue, magenta, cyan, white, bright.</p>
 *
 * @param foreground text color (null = terminal default)
 * @param background background color (null = transparent)
 * @param bold       bold text
 * @param italic     italic text
 */
public record StyleConfig(
        String foreground,
        String background,
        boolean bold,
        boolean italic
) {
    /**
     * Default style — no colors or modifiers.
     */
    public static final StyleConfig DEFAULT = new StyleConfig(null, null, false, false);

    /**
     * Converts this config to {@link AttributedStyle} for use with jline.
     *
     * @return AttributedStyle with the configured colors and modifiers
     */
    public AttributedStyle toAttributedStyle() {
        AttributedStyle style = AttributedStyle.DEFAULT;

        if (foreground != null) {
            int color = parseColor(foreground);
            if (color >= 0) {
                style = style.foreground(color);
            }
        }

        if (background != null) {
            int color = parseColor(background);
            if (color >= 0) {
                style = style.background(color);
            }
        }

        if (bold) {
            style = style.bold();
        }

        if (italic) {
            style = style.italic();
        }

        return style;
    }

    /**
     * Returns the numeric background color code.
     *
     * <p>Used in {@link PromptBuilder} to build separator style:
     * separator foreground = previous segment's background.</p>
     *
     * @return color code (constant from {@link AttributedStyle}) or -1 if not set
     */
    public int getBackgroundColor() {
        return background != null ? parseColor(background) : -1;
    }

    /**
     * Returns the numeric foreground color code.
     *
     * @return color code (constant from {@link AttributedStyle}) or -1 if not set
     */
    public int getForegroundColor() {
        return foreground != null ? parseColor(foreground) : -1;
    }

    private static int parseColor(String colorName) {
        if (colorName == null) return -1;
        return switch (colorName.toLowerCase()) {
            case "black" -> AttributedStyle.BLACK;
            case "red" -> AttributedStyle.RED;
            case "green" -> AttributedStyle.GREEN;
            case "yellow" -> AttributedStyle.YELLOW;
            case "blue" -> AttributedStyle.BLUE;
            case "magenta" -> AttributedStyle.MAGENTA;
            case "cyan" -> AttributedStyle.CYAN;
            case "white" -> AttributedStyle.WHITE;
            case "bright" -> AttributedStyle.BRIGHT;
            default -> -1;
        };
    }
}
