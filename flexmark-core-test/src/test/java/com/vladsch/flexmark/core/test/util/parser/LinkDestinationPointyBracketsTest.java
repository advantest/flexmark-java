package com.vladsch.flexmark.core.test.util.parser;

import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.DataHolder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * CommonMark 0.29 (#503, #538, #562): spaces are allowed inside a link destination in pointy brackets, and a
 * destination which is not in pointy brackets may not start with a <.
 */
public class LinkDestinationPointyBracketsTest {
    private static final ParserEmulationProfile[] PRE_029 = {
            ParserEmulationProfile.COMMONMARK_0_26,
            ParserEmulationProfile.COMMONMARK_0_27,
            ParserEmulationProfile.COMMONMARK_0_28,
            ParserEmulationProfile.COMMONMARK_LATEST,
    };

    private static String render(ParserEmulationProfile profile, String markdown) {
        DataHolder options = profile.getProfileOptions().toMutable().set(HtmlRenderer.PERCENT_ENCODE_URLS, true).toImmutable();
        return HtmlRenderer.builder(options).build().render(Parser.builder(options).build().parse(markdown));
    }

    private static void assert029(String expected, String markdown) {
        assertEquals(expected, render(ParserEmulationProfile.COMMONMARK_0_29, markdown));
    }

    private static void assertPre029(String expected, String markdown) {
        for (ParserEmulationProfile profile : PRE_029) {
            assertEquals("profile " + profile, expected, render(profile, markdown));
        }
    }

    @Test
    public void spaceInPointyBracketsIn029() {
        assert029("<p><a href=\"/my%20uri\">link</a></p>\n", "[link](</my uri>)\n");
        assert029("<p><a href=\"b%20%22c%22\">a</a></p>\n", "[a](<b \"c\">)\n");
        assert029("<p><img src=\"/my%20uri.png\" alt=\"img\" /></p>\n", "![img](</my uri.png>)\n");
    }

    @Test
    public void spaceInPointyBracketsNotAllowedBefore029() {
        assertPre029("<p>[link](&lt;/my uri&gt;)</p>\n", "[link](</my uri>)\n");
        assertPre029("<p>![img](&lt;/my uri.png&gt;)</p>\n", "![img](</my uri.png>)\n");
    }

    @Test
    public void lineBreakInPointyBracketsNeverAllowed() {
        String markdown = "[link](<foo\nbar>)\n";
        assert029("<p>[link](<foo\nbar>)</p>\n", markdown);
    }

    @Test
    public void escapedClosingPointyBracketIn029() {
        assert029("<p>[link](&lt;foo&gt;)</p>\n", "[link](<foo\\>)\n");
    }

    @Test
    public void escapedClosingPointyBracketBefore029() {
        assertPre029("<p><a href=\"foo%5C\">link</a></p>\n", "[link](<foo\\>)\n");
    }

    @Test
    public void bareDestinationMayNotStartWithPointyBracketIn029() {
        assert029("<p>[a](&lt;b)c\n[a](&lt;b)c&gt;\n[a](<b>c)</p>\n", "[a](<b)c\n[a](<b)c>\n[a](<b>c)\n");
    }

    @Test
    public void bareDestinationMayStartWithPointyBracketBefore029() {
        assertPre029("<p><a href=\"%3Cb\">a</a>c\n[a](&lt;b)c&gt;\n[a](<b>c)</p>\n", "[a](<b)c\n[a](<b)c>\n[a](<b>c)\n");
    }

    @Test
    public void wellFormedPointyBracketsAreUnchanged() {
        assertPre029("<p><a href=\"b)c\">a</a></p>\n", "[a](<b)c>)\n");
        assert029("<p><a href=\"b)c\">a</a></p>\n", "[a](<b)c>)\n");
        assertPre029("<p><a href=\"/url\" title=\"title\">a</a></p>\n", "[a](</url> \"title\")\n");
        assert029("<p><a href=\"/url\" title=\"title\">a</a></p>\n", "[a](</url> \"title\")\n");
    }

    // decision 9: the parser does not rewrite text, the link keeps pointing at its source range
    @Test
    public void sourcePositionsAreKeptForDestinationWithSpaceIn029() {
        DataHolder options = ParserEmulationProfile.COMMONMARK_0_29.getProfileOptions().toImmutable();
        String markdown = "x [a](<b c>) y\n";
        Document document = Parser.builder(options).build().parse(markdown);
        Link link = (Link) document.getFirstChild().getChildOfType(Link.class);

        assertEquals("[a](<b c>)", link.getChars().toString());
        assertEquals(2, link.getStartOffset());
        assertEquals(12, link.getEndOffset());

        assertEquals("<", link.getUrlOpeningMarker().toString());
        assertEquals(6, link.getUrlOpeningMarker().getStartOffset());
        assertEquals("b c", link.getUrl().toString());
        assertEquals(7, link.getUrl().getStartOffset());
        assertEquals(10, link.getUrl().getEndOffset());
        assertEquals(">", link.getUrlClosingMarker().toString());
        assertEquals(10, link.getUrlClosingMarker().getStartOffset());

        assertSame(document.getChars().getBaseSequence(), link.getUrl().getBaseSequence());
        assertEquals("b c", markdown.substring(link.getUrl().getStartOffset(), link.getUrl().getEndOffset()));
    }

    @Test
    public void formatterKeepsPointyBracketsOfDestinationWithSpaceIn029() {
        DataHolder options = ParserEmulationProfile.COMMONMARK_0_29.getProfileOptions().toImmutable();
        String markdown = "[a](<b c> \"t\") and ![i](<d e.png>) and [r][ref]\n\n[ref]: <f g> \"t\"\n";
        String formatted = Formatter.builder(options).build().render(Parser.builder(options).build().parse(markdown));
        assertEquals(markdown.trim(), formatted.trim());
    }
}
