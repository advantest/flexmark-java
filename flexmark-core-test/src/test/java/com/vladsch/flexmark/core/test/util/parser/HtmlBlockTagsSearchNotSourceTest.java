package com.vladsch.flexmark.core.test.util.parser;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * CommonMark 0.31 changed the list of tag names that start an HTML block of type 6: {@code search} was added and
 * {@code source} was removed. No spec example covers either tag, so they are tested here.
 */
public class HtmlBlockTagsSearchNotSourceTest {
    private static final ParserEmulationProfile[] NEW_BEHAVIOUR = {
            null,
            ParserEmulationProfile.COMMONMARK_0_31_2,
    };

    private static final ParserEmulationProfile[] OLD_BEHAVIOUR = {
            ParserEmulationProfile.COMMONMARK_0_26,
            ParserEmulationProfile.COMMONMARK_0_27,
            ParserEmulationProfile.COMMONMARK_0_28,
            ParserEmulationProfile.COMMONMARK_0_29,
            ParserEmulationProfile.COMMONMARK_0_30,
    };

    private static MutableDataSet base(ParserEmulationProfile profile, boolean deepParser) {
        MutableDataSet options = new MutableDataSet(profile == null ? null : profile.getProfileOptions());
        if (deepParser) options.set(Parser.HTML_BLOCK_DEEP_PARSER, true);
        return options;
    }

    private static String render(DataHolder options, String markdown) {
        return HtmlRenderer.builder(options).build().render(Parser.builder(options).build().parse(markdown));
    }

    private static String render(ParserEmulationProfile profile, boolean deepParser, String markdown) {
        return render(base(profile, deepParser).toImmutable(), markdown);
    }

    private static String format(ParserEmulationProfile profile, boolean deepParser, String markdown) {
        DataHolder options = base(profile, deepParser).toImmutable();
        return Formatter.builder(options).build().render(Parser.builder(options).build().parse(markdown));
    }

    // the formatter separates an HTML block from a preceding paragraph and ends the document with a blank line
    private static String norm(String markdown) {
        return markdown.trim().replace("\n\n", "\n") + "\n";
    }

    private static void assertNew(String expected, String markdown) {
        for (boolean deep : new boolean[] {false, true}) {
            for (ParserEmulationProfile profile : NEW_BEHAVIOUR) {
                assertEquals(profile + " deep=" + deep, expected, render(profile, deep, markdown));
            }
        }
    }

    private static void assertNewNoDeep(String expected, String markdown) {
        for (ParserEmulationProfile profile : NEW_BEHAVIOUR) {
            assertEquals(String.valueOf(profile), expected, render(profile, false, markdown));
        }
    }

    private static void assertOldNoDeep(String expected, String markdown) {
        for (ParserEmulationProfile profile : OLD_BEHAVIOUR) {
            assertEquals(String.valueOf(profile), expected, render(profile, false, markdown));
        }
    }

    private static void assertOld(String expected, String markdown) {
        for (boolean deep : new boolean[] {false, true}) {
            for (ParserEmulationProfile profile : OLD_BEHAVIOUR) {
                assertEquals(profile + " deep=" + deep, expected, render(profile, deep, markdown));
            }
        }
    }

    @Test
    public void keyIsOnByDefaultAndOnlyKeptByTheCommonMark0312Profile() {
        assertTrue(Parser.HTML_BLOCK_TAGS_SEARCH_NOT_SOURCE.get(null));
        for (ParserEmulationProfile profile : ParserEmulationProfile.values()) {
            assertEquals("profile " + profile, profile == ParserEmulationProfile.COMMONMARK_0_31_2,
                    Parser.HTML_BLOCK_TAGS_SEARCH_NOT_SOURCE.get(profile.getProfileOptions()));
        }
    }

    @Test
    public void tagListContainsExactlyOneOfTheTwo() {
        assertTrue(Parser.HTML_BLOCK_TAGS.get(null).contains("search"));
        assertFalse(Parser.HTML_BLOCK_TAGS.get(null).contains("source"));
        assertTrue(Parser.HTML_BLOCK_TAGS.get(ParserEmulationProfile.COMMONMARK_0_31_2.getProfileOptions()).contains("search"));
        assertFalse(Parser.HTML_BLOCK_TAGS.get(ParserEmulationProfile.COMMONMARK_0_31_2.getProfileOptions()).contains("source"));
        for (ParserEmulationProfile profile : OLD_BEHAVIOUR) {
            assertFalse(String.valueOf(profile), Parser.HTML_BLOCK_TAGS.get(profile.getProfileOptions()).contains("search"));
            assertTrue(String.valueOf(profile), Parser.HTML_BLOCK_TAGS.get(profile.getProfileOptions()).contains("source"));
        }
    }

    @Test
    public void commonMark0312ProfileKeepsAllCommonMark030And0312Rules() {
        assertTrue(Parser.HTML_COMMENT_ANY_TEXT.get(ParserEmulationProfile.COMMONMARK_0_31_2.getProfileOptions()));
        assertTrue(Parser.HTML_BLOCK_TEXTAREA_TYPE_1.get(ParserEmulationProfile.COMMONMARK_0_31_2.getProfileOptions()));
    }

    // --- new in 0.31 ---

    @Test
    public void searchStartsAnHtmlBlock() {
        assertNew("<search>*foo*\n", "<search>*foo*\n");
        assertNew("<search>\n*foo*\n", "<search>\n*foo*\n");
        assertNew("<search class=\"a\">*foo*\n", "<search class=\"a\">*foo*\n");
        assertNew("<SEARCH>*foo*\n", "<SEARCH>*foo*\n");
    }

    @Test
    public void closingSearchStartsAnHtmlBlock() {
        assertNew("</search>*foo*\n", "</search>*foo*\n");
        assertNew("</search>\n*foo*\n", "</search>\n*foo*\n");
    }

    @Test
    public void searchBlockEndsAtBlankLine() {
        assertNew("<search>\n*foo*\n<p><em>bar</em></p>\n", "<search>\n*foo*\n\n*bar*\n");
    }

    @Test
    public void searchInterruptsAParagraph() {
        assertNew("<p>foo</p>\n<search>\n*bar*\n", "foo\n<search>\n*bar*\n");
        assertNew("<p>foo</p>\n</search>\n*bar*\n", "foo\n</search>\n*bar*\n");
    }

    @Test
    public void sourceDoesNotStartAnHtmlBlock() {
        assertNew("<p><source><em>foo</em></p>\n", "<source>*foo*\n");
        assertNew("<p></source><em>foo</em></p>\n", "</source>*foo*\n");
    }

    // a complete tag alone on its line starts an HTML block of type 7, whatever its name (the deep parser has no type 7)
    @Test
    public void tagAloneOnALineStartsAnHtmlBlockOfType7() {
        assertNewNoDeep("<source>\n*foo*\n", "<source>\n*foo*\n");
        assertOldNoDeep("<search>\n*foo*\n", "<search>\n*foo*\n");
    }

    @Test
    public void sourceDoesNotInterruptAParagraph() {
        assertNew("<p>foo\n<source>\n<em>bar</em></p>\n", "foo\n<source>\n*bar*\n");
        assertNew("<p>foo\n</source>\n<em>bar</em></p>\n", "foo\n</source>\n*bar*\n");
    }

    @Test
    public void sourceInsideMediaElementStaysOneHtmlBlock() {
        assertEquals("<video controls>\n<source src=\"a.mp4\">\n</video>\n", render(null, false, "<video controls>\n<source src=\"a.mp4\">\n</video>\n"));
        assertEquals("<video controls>\n<source src=\"a.mp4\">\n</video>\n", render(ParserEmulationProfile.COMMONMARK_0_31_2, false, "<video controls>\n<source src=\"a.mp4\">\n</video>\n"));
    }

    // --- 0.30 and older keep the old list ---

    @Test
    public void oldProfilesDoNotStartAnHtmlBlockWithSearch() {
        assertOld("<p><search><em>foo</em></p>\n", "<search>*foo*\n");
        assertOld("<p></search><em>foo</em></p>\n", "</search>*foo*\n");
    }

    @Test
    public void oldProfilesDoNotInterruptAParagraphWithSearch() {
        assertOld("<p>foo\n<search>\n<em>bar</em></p>\n", "foo\n<search>\n*bar*\n");
    }

    @Test
    public void oldProfilesStartAnHtmlBlockWithSource() {
        assertOld("<source>*foo*\n", "<source>*foo*\n");
        assertOld("</source>*foo*\n", "</source>*foo*\n");
    }

    // the deep parser never lets a void tag such as source interrupt a paragraph, with any tag list
    @Test
    public void oldProfilesInterruptAParagraphWithSource() {
        assertOldNoDeep("<p>foo</p>\n<source>\n*bar*\n", "foo\n<source>\n*bar*\n");
    }

    // --- user supplied HTML_BLOCK_TAGS always wins ---

    @Test
    public void userSuppliedTagListIsUsedExactly() {
        for (ParserEmulationProfile profile : new ParserEmulationProfile[] {null, ParserEmulationProfile.COMMONMARK_0_31_2, ParserEmulationProfile.COMMONMARK_0_30}) {
            MutableDataSet only = base(profile, false).set(Parser.HTML_BLOCK_TAGS, Arrays.asList("div", "source"));
            assertEquals(String.valueOf(profile), Arrays.asList("div", "source"), Parser.HTML_BLOCK_TAGS.get(only));
            assertEquals(String.valueOf(profile), "<source>*foo*\n", render(only.toImmutable(), "<source>*foo*\n"));
            assertEquals(String.valueOf(profile), "<p><search><em>foo</em></p>\n", render(only.toImmutable(), "<search>*foo*\n"));

            MutableDataSet searchOnly = base(profile, false).set(Parser.HTML_BLOCK_TAGS, Arrays.asList("div", "search"));
            assertEquals(String.valueOf(profile), "<search>*foo*\n", render(searchOnly.toImmutable(), "<search>*foo*\n"));
            assertEquals(String.valueOf(profile), "<p><source><em>foo</em></p>\n", render(searchOnly.toImmutable(), "<source>*foo*\n"));
        }
    }

    @Test
    public void flagOnlyChangesTheDefaultOfTheTagList() {
        MutableDataSet options = new MutableDataSet().set(Parser.HTML_BLOCK_TAGS_SEARCH_NOT_SOURCE, false);
        assertEquals("<source>*foo*\n", render(options.toImmutable(), "<source>*foo*\n"));
        assertEquals("<p><search><em>foo</em></p>\n", render(options.toImmutable(), "<search>*foo*\n"));
        MutableDataSet on = new MutableDataSet(ParserEmulationProfile.COMMONMARK_0_30.getProfileOptions()).set(Parser.HTML_BLOCK_TAGS_SEARCH_NOT_SOURCE, true);
        assertEquals("<search>*foo*\n", render(on.toImmutable(), "<search>*foo*\n"));
        assertEquals("<p><source><em>foo</em></p>\n", render(on.toImmutable(), "<source>*foo*\n"));
    }

    // Parsers with different settings must not share state, in either order
    @Test
    public void configurationsDoNotLeakIntoEachOther() {
        for (int i = 0; i < 2; i++) {
            assertEquals("<search>*a*\n", render(ParserEmulationProfile.COMMONMARK_0_31_2, false, "<search>*a*\n"));
            assertEquals("<p><search><em>a</em></p>\n", render(ParserEmulationProfile.COMMONMARK_0_30, false, "<search>*a*\n"));
            assertEquals("<search>*a*\n", render(null, true, "<search>*a*\n"));
            assertEquals("<p><search><em>a</em></p>\n", render(ParserEmulationProfile.COMMONMARK_0_29, true, "<search>*a*\n"));
        }
    }

    // --- Markdown to Markdown ---

    @Test
    public void formatterKeepsSearchAndSourceBlocksAndInlines() {
        for (String md : new String[] {
                "<search>*foo*\n", "<search>\n*foo*\n", "</search>\n*foo*\n", "foo\n<search>\n*bar*\n",
                "<source>*foo*\n", "foo\n<source>\n*bar*\n", "*a* <source src=\"x\"> *b*\n", "*a* <search> *b*\n"}) {
            for (ParserEmulationProfile profile : NEW_BEHAVIOUR) {
                assertEquals(String.valueOf(profile), md, norm(format(profile, false, md)));
                assertEquals(render(profile, false, md), render(profile, false, format(profile, false, md)));
            }
        }
        for (ParserEmulationProfile profile : OLD_BEHAVIOUR) {
            String md = "<source>*foo*\n";
            assertEquals(String.valueOf(profile), md, norm(format(profile, false, md)));
            md = "foo\n<search>\n*bar*\n";
            assertEquals(String.valueOf(profile), md, norm(format(profile, false, md)));
        }
    }
}
