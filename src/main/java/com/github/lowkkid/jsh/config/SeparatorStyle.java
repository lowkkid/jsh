package com.github.lowkkid.jsh.config;

/**
 * Separator styles between prompt segments in powerline style.
 *
 * <p>Powerline is a styling approach that uses special characters from Nerd Fonts
 * to create "arrow-like" transitions between prompt segments:</p>
 *
 * <p>Requires a font with Powerline symbol support (e.g., Nerd Fonts, Powerline Fonts)
 * for correct rendering.</p>
 */
@SuppressWarnings("checkstyle:avoidescapedunicodecharacters")
public enum SeparatorStyle {
    /**
     * No separator — flat rectangular edge between segments.
     */
    FLAT(""),

    /**
     * Triangle separator (U+E0B0).
     */
    TRIANGLE("\uE0B0"),

    /**
     * Rounded separator (U+E0B4).
     */
    ROUNDED("\uE0B4");

    private final String symbol;

    SeparatorStyle(String symbol) {
        this.symbol = symbol;
    }

    /**
     * @return Unicode separator symbol or empty string for {@link #FLAT}
     */
    public String getSymbol() {
        return symbol;
    }
}
