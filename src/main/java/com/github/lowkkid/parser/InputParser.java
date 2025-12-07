package com.github.lowkkid.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InputParser {

    private final StringBuilder sb = new StringBuilder();
    private int index = 0;
    private String input;

    private boolean isWithinSingleQuotes = false;
    private boolean isWithinDoubleQuotes = false;
    private boolean isEscaping = false;
    private boolean isCommandQuoted = false;

    private String command = "";
    private final List<String> arguments = new ArrayList<>();

    public InputParser() {
    }

    public static InputParser getInstance() {
        return InputParserHolder.INSTANCE;
    }

    public CommandAndArgs getCommandAndArgs(String userInput) {
        reset();
        input = userInput.trim();

        char commandEndChar = ' ';

        if (input.charAt(0) == '\'') {
            index++;
            isCommandQuoted = true;
            commandEndChar = '\'';
        }

        if (input.charAt(0) == '"') {
            index++;
            isCommandQuoted = true;
            commandEndChar = '"';
        }

        while (index < input.length() && input.charAt(index) != commandEndChar) {
            sb.append(input.charAt(index++));
        }

        command = sb.toString();
        index = isCommandQuoted ? index + 2 : index + 1;
        sb.setLength(0);


        while (index < input.length()) {
            char currentChar = input.charAt(index++);

            if (isEscaping) {
                sb.append(currentChar);
                isEscaping = false;
                continue;
            }

            switch (currentChar) {
                case '\\' -> handleBackslash();
                case '\'' -> handleSingleQuote();
                case '"' -> handleDoubleQuote();
                case ' ' -> handleSpace();
                default -> sb.append(currentChar);
            }
        }
        arguments.add(sb.toString());
        return new CommandAndArgs(command, arguments);
    }

    private static final Set<Character> ESCAPED_CHARS_WITHIN_DOUBLE_QUOTES = Set.of('"', '\\', '$', '`');

    private void handleBackslash() {
        if (isWithinSingleQuotes) {
            sb.append("\\");
        } else if (isWithinDoubleQuotes) {
            if (ESCAPED_CHARS_WITHIN_DOUBLE_QUOTES.contains(input.charAt(index))) {
                isEscaping = true;
            } else {
                sb.append("\\");
            }
        } else {
            isEscaping = true;
        }
    }

    private void handleSpace() {
        if (!isWithinQuotes()) {
            if (!sb.isEmpty()) {
                arguments.add(sb.toString());
                sb.setLength(0);
            }
        } else {
            sb.append(' ');
        }
    }

    private void handleDoubleQuote() {
        if (isWithinSingleQuotes) {
            sb.append("\"");
            return;
        }

        isWithinDoubleQuotes = !isWithinDoubleQuotes;
    }

    private void handleSingleQuote() {
        if (isWithinDoubleQuotes) {
            sb.append("'");
            return;
        }

        isWithinSingleQuotes = !isWithinSingleQuotes;
    }

    private boolean isWithinQuotes() {
        return isWithinSingleQuotes || isWithinDoubleQuotes;
    }

    private void reset() {
        sb.setLength(0);
        index = 0;
        input = "";
        isEscaping = false;
        isCommandQuoted = false;
        isWithinSingleQuotes = false;
        isWithinDoubleQuotes = false;
        command = "";
        arguments.clear();
    }

    private static class InputParserHolder {
        private static final InputParser INSTANCE = new InputParser();
    }
}
