package com.vladsch.flexmark.core.test.util.parser;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * CommonMark 0.29 (#532, #569 and the simplified normalization): line endings in a code span become spaces, one
 * space is stripped from both ends unless the span consists only of spaces, interior whitespace is not collapsed.
 */
public class CodeSpanNormalizationTest {
    private static final ParserEmulationProfile[] PRE_029 = {
            ParserEmulationProfile.COMMONMARK_0_26,
            ParserEmulationProfile.COMMONMARK_0_27,
            ParserEmulationProfile.COMMONMARK_0_28,
            ParserEmulationProfile.COMMONMARK_LATEST,
    };

    private static String render(DataHolder options, String markdown) {
        return HtmlRenderer.builder(options).build().render(Parser.builder(options).build().parse(markdown));
    }

    private static String render(ParserEmulationProfile profile, String markdown) {
        return render(profile.getProfileOptions().toImmutable(), markdown);
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
    public void spanOfOnlySpacesIsKeptIn029() {
        assert029("<p><code> </code>\n<code>  </code></p>\n", "` `\n`  `\n");
    }

    @Test
    public void spanOfOnlySpacesIsEmptyBefore029() {
        assertPre029("<p><code></code>\n<code></code></p>\n", "` `\n`  `\n");
    }

    @Test
    public void spanOfOnlyLineEndingsIsOneSpaceIn029() {
        assert029("<p><code> </code></p>\n", "`\n`\n");
    }

    @Test
    public void onlyOneSpaceIsStrippedFromEachEndIn029() {
        assert029("<p><code>``</code></p>\n", "` `` `\n");
        assert029("<p><code> `` </code></p>\n", "`  ``  `\n");
        assert029("<p><code> a</code></p>\n", "` a`\n");
    }

    @Test
    public void oneSpaceIsNotStrippedBefore029() {
        assertPre029("<p><code>``</code></p>\n", "`  ``  `\n");
    }

    @Test
    public void lineEndingsBecomeSpacesAndAreNotCollapsedIn029() {
        assert029("<p><code>foo bar   baz</code></p>\n", "``\nfoo\nbar  \nbaz\n``\n");
        assert029("<p><code>foo </code></p>\n", "``\nfoo \n``\n");
        assert029("<p><code>foo   bar  baz</code></p>\n", "`foo   bar \nbaz`\n");
    }

    @Test
    public void interiorWhitespaceIsCollapsedBefore029() {
        assertPre029("<p><code>foo bar baz</code></p>\n", "``\nfoo\nbar  \nbaz\n``\n");
        assertPre029("<p><code>foo bar baz</code></p>\n", "`foo   bar \nbaz`\n");
    }

    @Test
    public void tabsAreNotSpacesIn029() {
        assert029("<p><code>\tb\t</code></p>\n", "`\tb\t`\n");
        assert029("<p><code>\t</code></p>\n", "`\t`\n");
        assert029("<p><code>a\t\tb</code></p>\n", "`a\t\tb`\n");
        assert029("<p><code>\ta </code></p>\n", "` \ta  `\n");
    }

    @Test
    public void tabsAreStrippedAndCollapsedBefore029() {
        assertPre029("<p><code>b</code></p>\n", "`\tb\t`\n");
        assertPre029("<p><code>a b</code></p>\n", "`a\t\tb`\n");
    }

    @Test
    public void codeSoftLineBreaksKeepBreaksAndStripLeadingAndTrailingLineEndingIn029() {
        DataHolder options = new MutableDataSet(ParserEmulationProfile.COMMONMARK_0_29.getProfileOptions())
                .set(Parser.CODE_SOFT_LINE_BREAKS, true)
                .set(HtmlRenderer.SOFT_BREAK, "\n")
                .toImmutable();
        assertEquals("<p><code>foo\nbar   baz</code></p>\n", render(options, "`foo\nbar   baz`\n"));
        assertEquals("<p><code>foo\nbar</code></p>\n", render(options, "``\nfoo\nbar\n``\n"));
        assertEquals("<p><code>foo  bar</code></p>\n", render(options, "` foo  bar `\n"));
        assertEquals("<p><code> </code></p>\n", render(options, "` `\n"));
    }

    @Test
    public void codeSoftLineBreaksStillCollapseBefore029() {
        DataHolder options = new MutableDataSet(ParserEmulationProfile.COMMONMARK_0_28.getProfileOptions())
                .set(Parser.CODE_SOFT_LINE_BREAKS, true)
                .set(HtmlRenderer.SOFT_BREAK, "\n")
                .toImmutable();
        assertEquals("<p><code>foo\nbar baz</code></p>\n", render(options, "`foo\nbar   baz`\n"));
    }

    @Test
    public void astKeepsRawSourceTextIn029() {
        DataHolder options = ParserEmulationProfile.COMMONMARK_0_29.getProfileOptions().toImmutable();
        Node code = Parser.builder(options).build().parse("``\nfoo  bar\n``\n").getFirstChild().getFirstChild();
        assertEquals("\nfoo  bar\n", code.getChildChars().toString());
    }

    @Test
    public void optionDefaultsToFalseAndIsSetOnlyBy029Profile() {
        assertFalse(Parser.CODE_SPAN_NORMALIZE_LINE_ENDINGS_AND_KEEP_INTERIOR_SPACES.get(null));
        for (ParserEmulationProfile profile : PRE_029) {
            assertFalse("profile " + profile,
                    Parser.CODE_SPAN_NORMALIZE_LINE_ENDINGS_AND_KEEP_INTERIOR_SPACES.get(profile.getProfileOptions()));
        }
        assertTrue(Parser.CODE_SPAN_NORMALIZE_LINE_ENDINGS_AND_KEEP_INTERIOR_SPACES
                .get(ParserEmulationProfile.COMMONMARK_0_29.getProfileOptions()));
    }

    @Test
    public void formatterRoundTripKeepsRenderedCodeSpansIn029() {
        DataHolder options = ParserEmulationProfile.COMMONMARK_0_29.getProfileOptions().toImmutable();
        for (String markdown : new String[] {"` `\n", "`  `\n", "``\nfoo\nbar  \nbaz\n``\n", "`  ``  `\n", "`\tb\t`\n", "`\n`\n"}) {
            String formatted = Formatter.builder(options).build().render(Parser.builder(options).build().parse(markdown));
            assertEquals(markdown, render(options, markdown), render(options, formatted));
        }
    }
}