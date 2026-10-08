package com.vladsch.flexmark.core.test.util.parser;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.test.specs.TestSpecLocator;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * CommonMark defines a line ending as a newline, a carriage return not followed by a newline, or a carriage return
 * and a following newline. A document therefore has to render identically no matter which of those a source file
 * uses, and no carriage return may ever leak into the rendered output.
 * <p>
 * Most renderers already normalize the raw source they emit, but reference links and reference images used to pass
 * their source through verbatim when the reference could not be resolved. Unlike the link text, a reference label is
 * never parsed into inline nodes, so a label spanning a line ending kept its carriage return and leaked it into the
 * output. This only ever showed on platforms that check out source files with CRLF line endings.
 */
final public class CrlfLineEndingTest {
    final private static Parser PARSER = Parser.builder().build();
    final private static HtmlRenderer RENDERER = HtmlRenderer.builder().escapeHtml(true).build();

    private static void assertRendersSameWithCrlf(String lfSource) {
        String crlfSource = lfSource.replace("\n", "\r\n");
        String expected = RENDERER.render(PARSER.parse(lfSource));
        String actual = RENDERER.render(PARSER.parse(crlfSource));

        assertEquals("CRLF source must render like LF source", expected, actual);
        assertEquals("no carriage return may leak into the output", -1, actual.indexOf('\r'));
    }

    @Test
    public void undefinedReferenceLinkLabelSpanningALineEnding() {
        assertRendersSameWithCrlf("a [text][undefined\nlabel] b\n");
    }

    @Test
    public void undefinedReferenceImageLabelSpanningALineEnding() {
        assertRendersSameWithCrlf("a ![text][undefined\nlabel] b\n");
    }

    @Test
    public void undefinedCollapsedReferenceLinkSpanningALineEnding() {
        assertRendersSameWithCrlf("a [undefined\nlabel][] b\n");
    }

    @Test
    public void undefinedShortcutReferenceLinkSpanningALineEnding() {
        assertRendersSameWithCrlf("a [undefined\nlabel] b\n");
    }

    @Test
    public void definedReferenceLinkLabelSpanningALineEnding() {
        assertRendersSameWithCrlf("a [text][defined\nlabel] b\n\n[defined label]: /url\n");
    }

    @Test
    public void paragraphWithASoftLineBreak() {
        assertRendersSameWithCrlf("first line\nsecond line\n");
    }

    @Test
    public void indentedCodeBlockKeepingItsContentVerbatim() {
        assertRendersSameWithCrlf("    code line one\n    code line two\n");
    }

    @Test
    public void fencedCodeBlockKeepingItsContentVerbatim() {
        assertRendersSameWithCrlf("```\ncode line one\ncode line two\n```\n");
    }

    @Test
    public void htmlBlockKeepingItsContentVerbatim() {
        assertRendersSameWithCrlf("<div>\n  <span>text</span>\n</div>\n");
    }

    /**
     * The full specification is the broadest sample of Markdown we have, so it doubles as an end to end check that
     * nothing anywhere in the renderer leaks a carriage return. This is the same property
     * {@code ParserTest.ioReaderTest} asserts, which fails on a CRLF checkout when a renderer regresses.
     */
    @Test
    public void theWholeSpecificationRendersTheSameWithCrlf() throws IOException {
        InputStream input = TestSpecLocator.DEFAULT_RESOURCE_LOCATION.getResourceInputStream();
        StringBuilder sb = new StringBuilder();
        try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer)) != -1) sb.append(buffer, 0, read);
        }

        String lfSource = sb.toString().replace("\r\n", "\n").replace('\r', '\n');
        assertRendersSameWithCrlf(lfSource);
    }
}
