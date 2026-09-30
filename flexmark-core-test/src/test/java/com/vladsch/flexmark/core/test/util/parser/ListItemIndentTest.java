package com.vladsch.flexmark.core.test.util.parser;

import com.vladsch.flexmark.ast.IndentedCodeBlock;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * CommonMark 0.29 (#497): a line indented four or more spaces, relative to the content of its container, cannot start
 * a list item. It continues the paragraph of the list item before it, or else it is an indented code block.
 */
public class ListItemIndentTest {
    private static final ParserEmulationProfile[] PRE_029 = {
            ParserEmulationProfile.COMMONMARK_0_26,
            ParserEmulationProfile.COMMONMARK_0_27,
            ParserEmulationProfile.COMMONMARK_0_28,
            ParserEmulationProfile.COMMONMARK_LATEST,
    };

    private static final String SPEC_282 = "- a\n - b\n  - c\n   - d\n    - e\n";
    private static final String SPEC_283 = "1. a\n\n  2. b\n\n    3. c\n";

    private static DataHolder options(ParserEmulationProfile profile) {
        return profile.getProfileOptions().toMutable().set(HtmlRenderer.PERCENT_ENCODE_URLS, true).toImmutable();
    }

    private static String render(ParserEmulationProfile profile, String markdown) {
        DataHolder options = options(profile);
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
    public void overIndentedItemIsParagraphContinuationIn029() {
        assert029("<ul>\n<li>a</li>\n<li>b</li>\n<li>c</li>\n<li>d\n- e</li>\n</ul>\n", SPEC_282);
    }

    @Test
    public void overIndentedItemStartsAnItemBefore029() {
        assertPre029("<ul>\n<li>a</li>\n<li>b</li>\n<li>c</li>\n<li>d</li>\n<li>e</li>\n</ul>\n", SPEC_282);
    }

    @Test
    public void overIndentedItemAfterBlankLineIsIndentedCodeIn029() {
        assert029("<ol>\n<li>\n<p>a</p>\n</li>\n<li>\n<p>b</p>\n</li>\n</ol>\n<pre><code>3. c\n</code></pre>\n", SPEC_283);
    }

    @Test
    public void overIndentedItemAfterBlankLineStartsAnItemBefore029() {
        assertPre029("<ol>\n<li>\n<p>a</p>\n</li>\n<li>\n<p>b</p>\n</li>\n<li>\n<p>c</p>\n</li>\n</ol>\n", SPEC_283);
    }

    @Test
    public void codeBlockEndsTheListAndFollowingItemsStartANewOneIn029() {
        assert029("<ul>\n<li>a</li>\n</ul>\n<pre><code>- b\n</code></pre>\n<ul>\n<li>c</li>\n</ul>\n",
                "  - a\n\n    - b\n\n- c\n");
    }

    @Test
    public void indentBelowFourIsStillAnItemIn029() {
        String markdown = "- a\n - b\n  - c\n   - d\n";
        String expected = "<ul>\n<li>a</li>\n<li>b</li>\n<li>c</li>\n<li>d</li>\n</ul>\n";
        assertPre029(expected, markdown);
        assert029(expected, markdown);
    }

    @Test
    public void sublistIndentedFourOrMoreFromParentItemIsUnchanged() {
        String markdown = "- a\n    - b\n";
        String expected = "<ul>\n<li>a\n<ul>\n<li>b</li>\n</ul>\n</li>\n</ul>\n";
        assertPre029(expected, markdown);
        assert029(expected, markdown);
    }

    @Test
    public void indentIsRelativeToBlockQuoteContentIn029() {
        String markdown = "> - a\n>  - b\n>   - c\n>    - d\n>     - e\n";
        assert029("<blockquote>\n<ul>\n<li>a</li>\n<li>b</li>\n<li>c</li>\n<li>d\n- e</li>\n</ul>\n</blockquote>\n", markdown);
        assertPre029("<blockquote>\n<ul>\n<li>a</li>\n<li>b</li>\n<li>c</li>\n<li>d</li>\n<li>e</li>\n</ul>\n</blockquote>\n", markdown);
    }

    @Test
    public void indentIsRelativeToParentItemContentNotToLineStartIn029() {
        // the inner items are at column 2..6 of the line, but only at 0..4 relative to the content of "- x"
        String markdown = "- x\n  - a\n   - b\n    - c\n     - d\n      - e\n";
        assert029("<ul>\n<li>x\n<ul>\n<li>a</li>\n<li>b</li>\n<li>c</li>\n<li>d\n- e</li>\n</ul>\n</li>\n</ul>\n", markdown);
        assertPre029("<ul>\n<li>x\n<ul>\n<li>a</li>\n<li>b</li>\n<li>c</li>\n<li>d</li>\n<li>e</li>\n</ul>\n</li>\n</ul>\n", markdown);
    }

    @Test
    public void itemIndentedFourInsideNestedListIsAbsolutelyBeyondFourButStillAnItemIn029() {
        // "- b" is at column 5 of the line, but only 3 relative to the content of "- x"
        String markdown = "- x\n  - a\n     - b\n";
        String expected = "<ul>\n<li>x\n<ul>\n<li>a\n<ul>\n<li>b</li>\n</ul>\n</li>\n</ul>\n</li>\n</ul>\n";
        assert029(expected, markdown);
        assertPre029(expected, markdown);
    }

    @Test
    public void tabIsFourColumnsIn029() {
        String markdown = "1. a\n  2. b\n\t3. c\n";
        assert029("<ol>\n<li>a</li>\n<li>b\n3. c</li>\n</ol>\n", markdown);
        assertPre029("<ol>\n<li>a</li>\n<li>b</li>\n<li>c</li>\n</ol>\n", markdown);
    }

    @Test
    public void tabAfterBlankLineIsIndentedCodeIn029() {
        String markdown = "1. a\n\n  2. b\n\n\t3. c\n";
        assert029("<ol>\n<li>\n<p>a</p>\n</li>\n<li>\n<p>b</p>\n</li>\n</ol>\n<pre><code>3. c\n</code></pre>\n", markdown);
    }

    @Test
    public void sourceTextIsNotRewrittenIn029() {
        DataHolder options = options(ParserEmulationProfile.COMMONMARK_0_29);
        Document document = Parser.builder(options).build().parse(SPEC_282);
        Paragraph paragraph = null;
        for (Node node = document.getFirstChild().getLastChild().getFirstChild(); node != null; node = node.getNext()) {
            if (node instanceof Paragraph) paragraph = (Paragraph) node;
        }
        assertNotNull(paragraph);
        assertSame(document.getChars().getBaseSequence(), paragraph.getChars().getBaseSequence());
        assertEquals(SPEC_282.indexOf("d\n"), paragraph.getStartOffset());
        assertEquals(SPEC_282.length(), paragraph.getEndOffset());
        assertEquals("d\n    - e\n", paragraph.getChars().toString());
        assertEquals(2, paragraph.getLineCount());
        assertEquals("    - e\n", paragraph.getLineChars(1).toString());

        document = Parser.builder(options).build().parse(SPEC_283);
        Node code = document.getLastChild();
        assertTrue(code instanceof IndentedCodeBlock);
        assertSame(document.getChars().getBaseSequence(), code.getChars().getBaseSequence());
        assertEquals(SPEC_283.indexOf("    3. c"), code.getStartOffset());
        assertEquals(SPEC_283.length(), code.getEndOffset());
    }

    @Test
    public void optionIsOnlySetByCommonMark029() {
        for (ParserEmulationProfile profile : ParserEmulationProfile.values()) {
            boolean expected = profile == ParserEmulationProfile.COMMONMARK_0_29;
            assertEquals("profile " + profile, expected, Parser.LISTS_NO_ITEM_AT_CODE_INDENT.get(profile.getProfileOptions()));
        }
        assertFalse(Parser.LISTS_NO_ITEM_AT_CODE_INDENT.getDefaultValue());
    }

    @Test
    public void formatterRoundTripIn029() {
        DataHolder options = options(ParserEmulationProfile.COMMONMARK_0_29);
        for (String markdown : new String[] { SPEC_282, SPEC_283, "1. a\n  2. b\n\t3. c\n", "- x\n  - a\n   - b\n    - c\n     - d\n      - e\n" }) {
            String formatted = Formatter.builder(options).build().render(Parser.builder(options).build().parse(markdown));
            assertEquals(markdown, render(ParserEmulationProfile.COMMONMARK_0_29, markdown), render(ParserEmulationProfile.COMMONMARK_0_29, formatted));
        }
    }
}
