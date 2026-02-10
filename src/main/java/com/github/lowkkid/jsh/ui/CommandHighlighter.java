package com.github.lowkkid.jsh.ui;

import com.github.lowkkid.jsh.command.utils.CommandRegistry;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

public class CommandHighlighter implements Highlighter {

    private final CommandRegistry registry;

    public CommandHighlighter(CommandRegistry registry) {
        this.registry = registry;
    }

    @Override
    public AttributedString highlight(LineReader reader, String buffer) {
        if (buffer.isEmpty()) {
            return AttributedString.EMPTY;
        }

        String[] parts = buffer.split("\\s", 2);
        String command = parts[0];
        boolean finishedTyping = parts.length > 1;

        AttributedStringBuilder sb = new AttributedStringBuilder();

        if (finishedTyping && registry.getExecutableCommand(command).isEmpty()) {
            sb.styled(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED), command);
        } else {
            sb.append(command);
        }

        if (parts.length > 1) {
            sb.append(buffer.substring(command.length()));
        }

        return sb.toAttributedString();
    }
}
