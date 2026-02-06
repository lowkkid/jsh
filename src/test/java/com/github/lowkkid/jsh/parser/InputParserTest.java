package com.github.lowkkid.jsh.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InputParserTest {

    private InputParser parser;

    @BeforeEach
    void setUp() {
        parser = new InputParser();
    }

    @Nested
    @DisplayName("getInstance()")
    class GetInstanceTests {

        @Test
        void returnsSingletonInstance() {
            InputParser instance1 = InputParser.getInstance();
            InputParser instance2 = InputParser.getInstance();

            assertSame(instance1, instance2);
        }
    }

    @Nested
    @DisplayName("Simple command parsing")
    class SimpleCommandTests {

        @Test
        void parsesSimpleCommand() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo");

            assertEquals(1, result.size());
            assertEquals("echo", result.getFirst().command());
            assertTrue(result.getFirst().arguments().isEmpty());
        }

        @Test
        void parsesCommandWithSingleArgument() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo hello");

            assertEquals(1, result.size());
            assertEquals("echo", result.getFirst().command());
            assertEquals(List.of("hello"), result.getFirst().arguments());
        }

        @Test
        void parsesCommandWithMultipleArguments() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo hello world");

            assertEquals(1, result.size());
            assertEquals("echo", result.getFirst().command());
            assertEquals(List.of("hello", "world"), result.getFirst().arguments());
        }

        @Test
        void trimsLeadingAndTrailingWhitespace() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("   echo hello   ");

            assertEquals(1, result.size());
            assertEquals("echo", result.getFirst().command());
            assertEquals(List.of("hello"), result.getFirst().arguments());
        }

        @Test
        void handlesMultipleSpacesBetweenArguments() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo    hello    world");

            assertEquals(1, result.size());
            assertEquals(List.of("hello", "world"), result.getFirst().arguments());
        }
    }

    @Nested
    @DisplayName("Quoted command parsing")
    class QuotedCommandTests {

        @Test
        void parsesCommandInSingleQuotes() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("'my command' arg1");

            assertEquals(1, result.size());
            assertEquals("my command", result.getFirst().command());
            assertEquals(List.of("arg1"), result.getFirst().arguments());
        }

        @Test
        void parsesCommandInDoubleQuotes() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("\"my command\" arg1");

            assertEquals(1, result.size());
            assertEquals("my command", result.getFirst().command());
            assertEquals(List.of("arg1"), result.getFirst().arguments());
        }
    }

    @Nested
    @DisplayName("Single quote handling in arguments")
    class SingleQuoteTests {

        @Test
        void preservesSpacesWithinSingleQuotes() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo 'hello world'");

            assertEquals(1, result.size());
            assertEquals(List.of("hello world"), result.getFirst().arguments());
        }

        @Test
        void preservesDoubleQuotesWithinSingleQuotes() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo 'hello \"world\"'");

            assertEquals(1, result.size());
            assertEquals(List.of("hello \"world\""), result.getFirst().arguments());
        }

        @Test
        void preservesBackslashWithinSingleQuotes() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo 'hello\\world'");

            assertEquals(1, result.size());
            assertEquals(List.of("hello\\world"), result.getFirst().arguments());
        }

        @Test
        void concatenatesQuotedAndUnquotedParts() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo 'hello''world'");

            assertEquals(1, result.size());
            assertEquals(List.of("helloworld"), result.getFirst().arguments());
        }

        @Test
        void ignoresEmptySingleQuotes() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo ''");

            assertEquals(1, result.size());
            assertTrue(result.getFirst().arguments().isEmpty());
        }

        @Test
        void handlesMultipleSingleQuotedArguments() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo 'arg 1' 'arg 2'");

            assertEquals(1, result.size());
            assertEquals(List.of("arg 1", "arg 2"), result.getFirst().arguments());
        }
    }

    @Nested
    @DisplayName("Double quote handling in arguments")
    class DoubleQuoteTests {

        @Test
        void preservesSpacesWithinDoubleQuotes() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo \"hello world\"");

            assertEquals(1, result.size());
            assertEquals(List.of("hello world"), result.getFirst().arguments());
        }

        @Test
        void preservesSingleQuotesWithinDoubleQuotes() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo \"hello 'world'\"");

            assertEquals(1, result.size());
            assertEquals(List.of("hello 'world'"), result.getFirst().arguments());
        }


        @Test
        void handlesEmptyDoubleQuotes() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo \"\"");

            assertEquals(1, result.size());
            assertTrue(result.getFirst().arguments().isEmpty());
        }

        @Test
        void handlesMultipleDoubleQuotedArguments() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo \"arg 1\" \"arg 2\"");

            assertEquals(1, result.size());
            assertEquals(List.of("arg 1", "arg 2"), result.getFirst().arguments());
        }
    }

    @Nested
    @DisplayName("Backslash escaping")
    class BackslashTests {

        @Test
        void escapesSpaceOutsideQuotes() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo hello\\ world");

            assertEquals(1, result.size());
            assertEquals(List.of("hello world"), result.getFirst().arguments());
        }

        @Test
        void escapesBackslashOutsideQuotes() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo hello\\\\world");

            assertEquals(1, result.size());
            assertEquals(List.of("hello\\world"), result.getFirst().arguments());
        }

        @Test
        void escapesDoubleQuoteInsideDoubleQuotes() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo \"hello\\\"world\"");

            assertEquals(1, result.size());
            assertEquals(List.of("hello\"world"), result.getFirst().arguments());
        }

        @Test
        void escapesBackslashInsideDoubleQuotes() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo \"hello\\\\world\"");

            assertEquals(1, result.size());
            assertEquals(List.of("hello\\world"), result.getFirst().arguments());
        }

        @Test
        void escapesDollarSignInsideDoubleQuotes() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo \"hello\\$world\"");

            assertEquals(1, result.size());
            assertEquals(List.of("hello$world"), result.getFirst().arguments());
        }

        @Test
        void escapesBacktickInsideDoubleQuotes() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo \"hello\\`world\"");

            assertEquals(1, result.size());
            assertEquals(List.of("hello`world"), result.getFirst().arguments());
        }

        @Test
        void preservesBackslashForNonSpecialCharsInsideDoubleQuotes() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo \"hello\\nworld\"");

            assertEquals(1, result.size());
            assertEquals(List.of("hello\\nworld"), result.getFirst().arguments());
        }

        @Test
        void backslashIsLiteralInsideSingleQuotes() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo 'hello\\\"world'");

            assertEquals(1, result.size());
            assertEquals(List.of("hello\\\"world"), result.getFirst().arguments());
        }
    }

    @Nested
    @DisplayName("Pipeline parsing")
    class PipelineTests {

        @Test
        void parsesTwoCommandPipeline() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo hello | cat");

            assertEquals(2, result.size());
            assertEquals("echo", result.getFirst().command());
            assertEquals(List.of("hello"), result.getFirst().arguments());
            assertEquals("cat", result.get(1).command());
            assertTrue(result.get(1).arguments().isEmpty());
        }

        @Test
        void parsesThreeCommandPipeline() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("cat file | grep pattern | wc -l");

            assertEquals(3, result.size());
            assertEquals("cat", result.getFirst().command());
            assertEquals("grep", result.get(1).command());
            assertEquals("wc", result.get(2).command());
            assertEquals(List.of("-l"), result.get(2).arguments());
        }

        @Test
        void handlesPipeWithMultipleSpaces() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo hello   |   cat");

            assertEquals(2, result.size());
            assertEquals("echo", result.getFirst().command());
            assertEquals("cat", result.get(1).command());
        }
    }

    @Nested
    @DisplayName("Redirect parsing - stdout rewrite (>)")
    class StdoutRewriteRedirectTests {

        @Test
        void parsesSimpleRedirect() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo hello > file.txt");

            assertEquals(1, result.size());
            assertTrue(result.getFirst().shouldBeRedirected());
            assertEquals("file.txt", result.getFirst().redirectOptions().redirectTo());
            assertEquals(RedirectOptions.RedirectType.REWRITE, result.getFirst().redirectOptions().redirectType());
            assertEquals(RedirectOptions.RedirectStream.STDOUT, result.getFirst().redirectOptions().redirectStream());
        }

        @Test
        void parsesExplicitStdoutRedirect() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo hello 1> file.txt");

            assertEquals(1, result.size());
            assertTrue(result.getFirst().shouldBeRedirected());
            assertEquals("file.txt", result.getFirst().redirectOptions().redirectTo());
            assertEquals(RedirectOptions.RedirectStream.STDOUT, result.getFirst().redirectOptions().redirectStream());
        }

        @Test
        void parsesRedirectWithoutSpace() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo hello >file.txt");

            assertEquals(1, result.size());
            assertTrue(result.getFirst().shouldBeRedirected());
            assertEquals("file.txt", result.getFirst().redirectOptions().redirectTo());
        }
    }

    @Nested
    @DisplayName("Redirect parsing - stdout append (>>)")
    class StdoutAppendRedirectTests {

        @Test
        void parsesAppendRedirect() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo hello >> file.txt");

            assertEquals(1, result.size());
            assertTrue(result.getFirst().shouldBeRedirected());
            assertEquals("file.txt", result.getFirst().redirectOptions().redirectTo());
            assertEquals(RedirectOptions.RedirectType.APPEND, result.getFirst().redirectOptions().redirectType());
        }

        @Test
        void parsesExplicitStdoutAppendRedirect() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo hello 1>> file.txt");

            assertEquals(1, result.size());
            assertEquals(RedirectOptions.RedirectType.APPEND, result.getFirst().redirectOptions().redirectType());
            assertEquals(RedirectOptions.RedirectStream.STDOUT, result.getFirst().redirectOptions().redirectStream());
        }
    }

    @Nested
    @DisplayName("Redirect parsing - stderr (2>)")
    class StderrRedirectTests {

        @Test
        void parsesStderrRewriteRedirect() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("command 2> error.log");

            assertEquals(1, result.size());
            assertTrue(result.getFirst().shouldBeRedirected());
            assertEquals("error.log", result.getFirst().redirectOptions().redirectTo());
            assertEquals(RedirectOptions.RedirectStream.STDERR, result.getFirst().redirectOptions().redirectStream());
            assertEquals(RedirectOptions.RedirectType.REWRITE, result.getFirst().redirectOptions().redirectType());
        }

        @Test
        void parsesStderrAppendRedirect() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("command 2>> error.log");

            assertEquals(1, result.size());
            assertEquals(RedirectOptions.RedirectStream.STDERR, result.getFirst().redirectOptions().redirectStream());
            assertEquals(RedirectOptions.RedirectType.APPEND, result.getFirst().redirectOptions().redirectType());
        }
    }

    @Nested
    @DisplayName("Redirect with no redirection")
    class NoRedirectTests {

        @Test
        void commandWithoutRedirectHasNullRedirectOptions() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo hello");

            assertEquals(1, result.size());
            assertFalse(result.getFirst().shouldBeRedirected());
            assertNull(result.getFirst().redirectOptions());
        }
    }


    @Nested
    @DisplayName("Complex scenarios")
    class ComplexScenarioTests {

        @Test
        void pipelineWithRedirect() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("cat file.txt | grep pattern > output.txt");

            assertEquals(2, result.size());
            assertEquals("cat", result.getFirst().command());
            assertFalse(result.getFirst().shouldBeRedirected());
            assertEquals("grep", result.get(1).command());
            assertTrue(result.get(1).shouldBeRedirected());
            assertEquals("output.txt", result.get(1).redirectOptions().redirectTo());
        }

        @Test
        void quotedArgumentsWithSpecialCharacters() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo \"$HOME\" '$PATH'");

            assertEquals(1, result.size());
            assertEquals(List.of("$HOME", "$PATH"), result.getFirst().arguments());
        }

        @Test
        void escapedCharactersOutsideQuotes() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo \\$HOME \\\"quoted\\\"");

            assertEquals(1, result.size());
            assertEquals(List.of("$HOME", "\"quoted\""), result.getFirst().arguments());
        }

        @Test
        void multipleConsecutiveEscapes() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo \\\\\\\\");

            assertEquals(1, result.size());
            assertEquals(List.of("\\\\"), result.getFirst().arguments());
        }

        @Test
        void parserStateResetsBetweenCalls() {
            parser.getCommandAndArgs("echo 'partial");
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo hello");

            assertEquals(1, result.size());
            assertEquals("echo", result.getFirst().command());
            assertEquals(List.of("hello"), result.getFirst().arguments());
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        void commandWithNoArguments() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("pwd");

            assertEquals(1, result.size());
            assertEquals("pwd", result.getFirst().command());
            assertTrue(result.getFirst().arguments().isEmpty());
        }

        @Test
        void argumentEndingWithEscape() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo test\\");

            assertEquals(1, result.size());
            assertEquals(List.of("test"), result.getFirst().arguments());
        }

        @Test
        void redirectToPathWithDirectory() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo hello > /tmp/output.txt");

            assertEquals(1, result.size());
            assertEquals("/tmp/output.txt", result.getFirst().redirectOptions().redirectTo());
        }

        @Test
        void pipelineWithQuotedPipe() {
            // Current parser behavior: pipe is not checked for quotes context
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo 'a|b' | cat");

            assertEquals(3, result.size());
            assertEquals("echo", result.getFirst().command());
            assertEquals("b'", result.get(1).command());
            assertEquals("cat", result.get(2).command());
        }

        @Test
        void consecutiveSpacesOutsideQuotes() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo    a    b    c");

            assertEquals(1, result.size());
            assertEquals(List.of("a", "b", "c"), result.getFirst().arguments());
        }

        @Test
        void redirectWithConsecutiveSpaces() {
            List<CommandAndArgs> result = parser.getCommandAndArgs("echo hello >    file.txt");

            assertEquals(1, result.size());
            assertEquals("file.txt", result.getFirst().redirectOptions().redirectTo());
        }
    }
}
