package com.vladsch.flexmark.core.test.util.parser;

import org.junit.Test;

import static com.vladsch.flexmark.core.test.util.parser.Profile030Support.assert030;
import static com.vladsch.flexmark.core.test.util.parser.Profile030Support.assertPre030;

/**
 * CommonMark 0.30 (#620): a declaration is {@code <!}, an ASCII letter, zero or more characters other than
 * {@code >}, and {@code >}. Whitespace after the name is no longer required. HTML blocks of type 4 start with
 * {@code <!} followed by an ASCII letter, not only an uppercase letter.
 */
public class HtmlDeclarationTest {
    @Test
    public void inlineDeclarationWithoutWhitespaceIn030() {
        assert030("<p>a <!FOO> b</p>\n", "a <!FOO> b\n");
    }

    @Test
    public void inlineDeclarationWithoutWhitespaceIsTextBefore030() {
        assertPre030("<p>a &lt;!FOO&gt; b</p>\n", "a <!FOO> b\n");
    }

    @Test
    public void inlineDeclarationWithLowerCaseNameWithoutWhitespaceIn030() {
        assert030("<p>a <!foo> b</p>\n", "a <!foo> b\n");
    }

    @Test
    public void inlineDeclarationWithSingleLetter() {
        assert030("<p>a <!x> b</p>\n", "a <!x> b\n");
    }

    @Test
    public void inlineDeclarationWithContentIn030() {
        assert030("<p>a <!DOCTYPE html> b</p>\n", "a <!DOCTYPE html> b\n");
    }

    @Test
    public void inlineDeclarationWithContentStillWorksBefore030() {
        assertPre030("<p>a <!DOCTYPE html> b</p>\n", "a <!DOCTYPE html> b\n");
    }

    @Test
    public void inlineDeclarationMayContainAnything() {
        assert030("<p>a <!x \"q\" 1 &> b</p>\n", "a <!x \"q\" 1 &> b\n");
    }

    @Test
    public void declarationNeedsALetterAfterTheBang() {
        assert030("<p>a &lt;!&gt; b &lt;!1&gt; c &lt;! x&gt; d</p>\n", "a <!> b <!1> c <! x> d\n");
        assertPre030("<p>a &lt;!&gt; b &lt;!1&gt; c &lt;! x&gt; d</p>\n", "a <!> b <!1> c <! x> d\n");
    }

    @Test
    public void declarationNeedsAClosingBracket() {
        assert030("<p>a &lt;!foo b</p>\n", "a <!foo b\n");
    }

    @Test
    public void blockOfTypeFourMayStartWithALowerCaseLetterIn030() {
        assert030("<!doctype html>\n<p>foo</p>\n<p>bar</p>\n", "<!doctype html>\nfoo\n\nbar\n");
    }

    @Test
    public void lowerCaseDeclarationLineIsAParagraphBefore030() {
        assertPre030("<p><!doctype html>\nfoo</p>\n<p>bar</p>\n", "<!doctype html>\nfoo\n\nbar\n");
    }

    @Test
    public void blockOfTypeFourRunsUntilTheClosingBracket() {
        assert030("<!foo\n\nbar\nbaz>\n<p>qux</p>\n", "<!foo\n\nbar\nbaz>\nqux\n");
    }

    @Test
    public void blockOfTypeFourStartsWithoutWhitespace() {
        assert030("<!X>\n<p>foo</p>\n", "<!X>\nfoo\n");
    }

    @Test
    public void blockOfTypeFourNeedsALetter() {
        assert030("<p>&lt;!1&gt;\nfoo</p>\n", "<!1>\nfoo\n");
    }

    @Test
    public void blockOfTypeFourMayInterruptAParagraph() {
        assert030("<p>foo</p>\n<!doctype html>\n", "foo\n<!doctype html>\n");
    }
}