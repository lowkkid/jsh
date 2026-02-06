package com.github.lowkkid.jsh.config;

/**
 * Configuration for the command line prompt appearance.
 *
 * <p>Loaded from a JSON file via {@link PromptConfigReader}. Example config:</p>
 * <pre>
 * {
 *   "includeUser": true,
 *   "userStyle": { "foreground": "black", "background": "green", "bold": true },
 *   "userSeparator": "TRIANGLE",
 *   "pathStyle": { "foreground": "white", "background": "blue", "bold": true },
 *   "pathSeparator": "TRIANGLE",
 *   "promptSymbol": "$",
 *   "promptSymbolStyle": { "foreground": "white" }
 * }
 * </pre>
 *
 * @param includeUser       whether to show the username
 * @param userStyle         style for the username segment
 * @param userSeparator     separator style after the username
 * @param pathStyle         style for the current path segment
 * @param pathSeparator     separator style after the path
 * @param promptSymbol      prompt symbol (e.g., "$" or ">")
 * @param promptSymbolStyle style for the prompt symbol
 */
public record PromptConfig(
        boolean includeUser,
        StyleConfig userStyle,
        SeparatorStyle userSeparator,
        StyleConfig pathStyle,
        SeparatorStyle pathSeparator,
        String promptSymbol,
        StyleConfig promptSymbolStyle
) {

    /**
     * Default configuration.
     *
     * <p>Shows only the path (blue background, white text) with triangle separator.</p>
     *
     * @return default config
     */
    @SuppressWarnings("checkstyle:methodname")
    public static PromptConfig DEFAULT() {
        return new PromptConfig(
                false,
                new StyleConfig("black", "green", true, false),
                SeparatorStyle.TRIANGLE,
                new StyleConfig("white", "blue", true, false),
                SeparatorStyle.TRIANGLE,
                null,
                null
        );
    }

    /**
     * @return user style or {@link StyleConfig#DEFAULT} if not set
     */
    public StyleConfig userStyle() {
        return userStyle != null ? userStyle : StyleConfig.DEFAULT;
    }

    /**
     * @return path style or default (white on blue, bold) if not set
     */
    public StyleConfig pathStyle() {
        return pathStyle != null ? pathStyle : new StyleConfig("white", "blue", true, false);
    }

    /**
     * @return separator after user or {@link SeparatorStyle#TRIANGLE} if not set
     */
    public SeparatorStyle userSeparator() {
        return userSeparator != null ? userSeparator : SeparatorStyle.TRIANGLE;
    }

    /**
     * @return separator after path or {@link SeparatorStyle#TRIANGLE} if not set
     */
    public SeparatorStyle pathSeparator() {
        return pathSeparator != null ? pathSeparator : SeparatorStyle.TRIANGLE;
    }

    /**
     * @return prompt symbol style or {@link StyleConfig#DEFAULT} if not set
     */
    public StyleConfig promptSymbolStyle() {
        return promptSymbolStyle != null ? promptSymbolStyle : StyleConfig.DEFAULT;
    }
}
