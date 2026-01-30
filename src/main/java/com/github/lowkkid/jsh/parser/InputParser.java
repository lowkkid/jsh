package com.github.lowkkid.jsh.parser;

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
    private String redirectTo = null;
    private RedirectOptions.RedirectType redirectType = RedirectOptions.RedirectType.REWRITE;
    private RedirectOptions.RedirectStream redirectStream = RedirectOptions.RedirectStream.STDOUT;
    private final List<String> arguments = new ArrayList<>();

    public InputParser() {
    }

    public static InputParser getInstance() {
        return InputParserHolder.INSTANCE;
    }

    public CommandAndArgs getCommandAndArgs(String userInput) {
        reset();
        input = userInput.trim();
        parseCommand();
        parseArguments();
        RedirectOptions redirectOptions = null;
        if (redirectTo != null) {
            redirectOptions = new RedirectOptions(redirectTo, redirectType, redirectStream);
        }
        return new CommandAndArgs(command, arguments, redirectOptions);
    }

    private void parseCommand() {
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
    }

    private void parseArguments() {
        while (index < input.length()) {
            char currentChar = input.charAt(index++);

            if (isEscaping) {
                sb.append(currentChar);
                isEscaping = false;
                continue;
            }

            switch (currentChar) {
                case '>' -> handleRedirect();
                case '\\' -> handleBackslash();
                case '\'' -> handleSingleQuote();
                case '"' -> handleDoubleQuote();
                case ' ' -> handleSpace();
                default -> sb.append(currentChar);
            }
        }
        if (!sb.isEmpty()) {
            arguments.add(sb.toString());
        }
    }

    private static final Set<Character> ESCAPED_CHARS_WITHIN_DOUBLE_QUOTES = Set.of('"', '\\', '$', '`');

    private void handleRedirect() {
        if (sb.length() == 1) {
            char outputStream = sb.charAt(0);
            if (outputStream == '1') {
                sb.setLength(0);
            }
            if (outputStream == '2') {
                redirectStream = RedirectOptions.RedirectStream.STDERR;
                sb.setLength(0);
            }
        }
        if ('>' == input.charAt(index)) {
            redirectType = RedirectOptions.RedirectType.APPEND;
            index++;
        }
        skipWhitespace();

        var redirectBuilder = new StringBuilder();

        while (index < input.length() && input.charAt(index) != ' ') {
            redirectBuilder.append(input.charAt(index++));
        }

        index++;
        redirectTo = redirectBuilder.toString();
    }

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

    private void skipWhitespace() {
        while (input.charAt(index) == ' ') {
            index++;
        }
    }

    private void reset() {
        redirectTo = null;
        redirectType = RedirectOptions.RedirectType.REWRITE;
        redirectStream = RedirectOptions.RedirectStream.STDOUT;
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
