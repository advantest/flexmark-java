package com.vladsch.flexmark.core.test.util.parser;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Reference;
import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.DataHolder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * CommonMark 0.29 (#395, #469, #526): a setext heading after link reference definitions, a title must be separated
 * from the destination by whitespace, and a parenthesized title may not contain an unescaped (.
 */
public class LinkReferenceDefinitionTest {
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
    public void titleMustBeSeparatedFromDestinationIn029() {
        assert029("<p>[foo]: <bar>(baz)</p>\n<p>[foo]</p>\n", "[foo]: <bar>(baz)\n\n[foo]\n");
    }

    @Test
    public void titleNeedNotBeSeparatedFromDestinationBefore029() {
        assertPre029("<p><a href=\"bar\" title=\"baz\">foo</a></p>\n", "[foo]: <bar>(baz)\n\n[foo]\n");
    }

    @Test
    public void titleSeparatedByWhitespaceIsUnchanged() {
        String markdown = "[foo]: <bar> (baz)\n[a]: /url\n'title'\n\n[foo] [a]\n";
        String expected = "<p><a href=\"bar\" title=\"baz\">foo</a> <a href=\"/url\" title=\"title\">a</a></p>\n";
        assertPre029(expected, markdown);
        assert029(expected, markdown);
    }

    @Test
    public void setextHeadingAfterDefinitionIn029() {
        assert029("<h1>bar</h1>\n<p><a href=\"/url\">foo</a></p>\n", "[foo]: /url\nbar\n===\n[foo]\n");
        assert029("<h2>bar</h2>\n<p><a href=\"/url\">foo</a></p>\n", "[foo]: /url\n[x]: /y\nbar\n---\n[foo]\n");
    }

    @Test
    public void setextHeadingAfterDefinitionBefore029() {
        assertPre029("<h1>[foo]: /url\nbar</h1>\n<p>[foo]</p>\n", "[foo]: /url\nbar\n===\n[foo]\n");
    }

    @Test
    public void underlineOfOnlyDefinitionsIsNotAHeadingIn029() {
        assert029("<p>===\n<a href=\"/url\">foo</a></p>\n", "[foo]: /url\n===\n[foo]\n");
        assert029("<hr />\n<p><a href=\"/url\">foo</a></p>\n", "[foo]: /url\n---\n[foo]\n");
    }

    @Test
    public void underlineOfOnlyDefinitionsIsAHeadingBefore029() {
        assertPre029("<h1>[foo]: /url</h1>\n<p>[foo]</p>\n", "[foo]: /url\n===\n[foo]\n");
    }

    @Test
    public void definitionOnlyParagraphIsNotVisible() {
        assertPre029("", "[foo]: /url\n");
        assert029("", "[foo]: /url\n");
    }

    @Test
    public void definitionsInSetextHeadingDoNotChangeFirstDefinitionWins() {
        assert029("<h1>foo</h1>\n<p><a href=\"/1\">a</a></p>\n", "[a]: /1\n\n[a]: /2\nfoo\n===\n\n[a]\n");
    }

    @Test
    public void definitionAfterHeadingTextStaysInHeading() {
        String expected = "<h1>foo\n[bar]: /u</h1>\n<p>[bar]</p>\n";
        assertPre029(expected, "foo\n[bar]: /u\n===\n\n[bar]\n");
        assert029(expected, "foo\n[bar]: /u\n===\n\n[bar]\n");
    }

    @Test
    public void multiLineTitleBeforeSetextHeadingIn029() {
        assert029("<h1>bar</h1>\n<p><a href=\"/url\" title=\"t\">foo</a></p>\n", "[foo]: /url\n't'\nbar\n===\n[foo]\n");
    }

    @Test
    public void parenthesizedTitleWithUnescapedOpeningParenthesisIn029() {
        assert029("<p>[foo]: /url (ti(tle)</p>\n<p>[foo]</p>\n", "[foo]: /url (ti(tle)\n\n[foo]\n");
        assert029("<p><a href=\"/url\" title=\"ti(tle\">foo</a></p>\n", "[foo]: /url (ti\\(tle)\n\n[foo]\n");
        assert029("<p>[a](/url (ti(tle))</p>\n", "[a](/url (ti(tle))\n");
    }

    @Test
    public void parenthesizedTitleWithUnescapedOpeningParenthesisBefore029() {
        assertPre029("<p><a href=\"/url\" title=\"ti(tle\">foo</a></p>\n", "[foo]: /url (ti(tle)\n\n[foo]\n");
        assertPre029("<p><a href=\"/url\" title=\"ti(tle\">a</a></p>\n", "[a](/url (ti(tle))\n");
    }

    // decision 9: the parser does not rewrite text, nodes keep pointing at their source ranges
    @Test
    public void sourcePositionsAreKeptForSetextHeadingAfterDefinitionIn029() {
        DataHolder options = ParserEmulationProfile.COMMONMARK_0_29.getProfileOptions().toImmutable();
        String markdown = "[foo]: /url \"t\"\nbar\n===\n[foo]\n";
        Document document = Parser.builder(options).build().parse(markdown);

        Reference reference = (Reference) document.getFirstChild();
        assertEquals("foo", reference.getReference().toString());
        assertEquals(1, reference.getReference().getStartOffset());
        assertEquals("/url", reference.getUrl().toString());
        assertEquals(7, reference.getUrl().getStartOffset());
        assertEquals("t", reference.getTitle().toString());
        assertEquals("\"", reference.getTitleOpeningMarker().toString());
        assertEquals(12, reference.getTitleOpeningMarker().getStartOffset());
        assertSame(document.getChars().getBaseSequence(), reference.getUrl().getBaseSequence());

        Heading heading = (Heading) reference.getNext();
        assertEquals(1, heading.getLevel());
        assertEquals("bar", heading.getText().toString());
        assertEquals(16, heading.getText().getStartOffset());
        assertEquals("===", heading.getClosingMarker().toString());
        assertEquals(20, heading.getClosingMarker().getStartOffset());
        assertEquals("bar\n===", heading.getChars().toString());
        assertEquals(16, heading.getStartOffset());
        assertSame(document.getChars().getBaseSequence(), heading.getText().getBaseSequence());
        assertTrue(reference.getEndOffset() <= heading.getStartOffset());
    }

    @Test
    public void formatterRoundTripOfDefinitionsAndSetextHeadingIn029() {
        DataHolder options = ParserEmulationProfile.COMMONMARK_0_29.getProfileOptions().toImmutable();
        String markdown = "[foo]: /url \"t\"\n[x]: /y\n\nbar\n===\n\n[foo]: /url\n===\n\n[foo] [x]\n";
        String formatted = Formatter.builder(options).build().render(Parser.builder(options).build().parse(markdown));
        String html = render(ParserEmulationProfile.COMMONMARK_0_29, markdown);
        assertEquals(html, render(ParserEmulationProfile.COMMONMARK_0_29, formatted));
    }
}
