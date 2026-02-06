package com.github.lowkkid.jsh.data;

public class MockData {

    public static final String MOCK_PROMPT_CONFIG = """
            {
              "includeUser": true,
              "userStyle": {
                "foreground": "black",
                "background": "green",
                "bold": true,
                "italic": true
              },
              "userSeparator": "TRIANGLE",
              "pathStyle": {
                "foreground": "white",
                "background": "blue",
                "bold": true,
                "italic": false
              },
              "pathSeparator": "TRIANGLE",
              "promptSymbol": "$",
              "promptSymbolStyle": {
                "foreground": "white",
                "background": "transparent",
                "bold": true,
                "italic": false
              }
            }
            """;
}
