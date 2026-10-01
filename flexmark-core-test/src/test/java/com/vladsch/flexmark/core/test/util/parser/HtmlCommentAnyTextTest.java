package com.vladsch.flexmark.core.test.util.parser;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * CommonMark 0.31: an inline HTML comment is {@code <!-->}, {@code <!--->} or {@code <!--}, a string of
 * characters not including {@code -->}, and {@code -->}. Text containing {@code --}, text ending in {@code -}
 * and text starting with {@code -} are all allowed. Before 0.31 the text could not start with {@code >} or
 * {@code ->}, could not contain {@code --} and could not end in {@code -}.
 * <p>
 * Spec examples 625 and 626 cover only two shapes, the remaining ones are tested here.
 */
public class HtmlCommentAnyTextTest {
    private static final DataHolder DEFAULT = new MutableDataSet().toImmutable();

    private static DataHolder options(ParserEmulationProfile profile) {
        return profile == null ? DEFAULT : profile.getProfileOptions().toImmutable();
    }

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

    private static String render(ParserEmulationProfile profile, String markdown) {
        DataHolder options = options(profile);
        return HtmlRenderer.builder(options).build().render(Parser.builder(options).build().parse(markdown));
    }

    private static String format(ParserEmulationProfile profile, String markdown) {
        DataHolder options = options(profile);
        return Formatter.builder(options).build().render(Parser.builder(options).build().parse(markdown));
    }

    private static void assertNew(String expected, String markdown) {
        for (ParserEmulationProfile profile : NEW_BEHAVIOUR) {
            assertEquals(String.valueOf(profile), expected, render(profile, markdown));
        }
    }

    private static void assertOld(String expected, String markdown) {
        for (ParserEmulationProfile profile : OLD_BEHAVIOUR) {
            assertEquals(String.valueOf(profile), expected, render(profile, markdown));
        }
    }

    private static void assertAll(String expected, String markdown) {
        assertNew(expected, markdown);
        assertOld(expected, markdown);
    }

    @Test
    public void keyIsOnByDefaultAndOnlyKeptByTheCommonMark0312Profile() {
        assertTrue(Parser.HTML_COMMENT_ANY_TEXT.get(null));
        for (ParserEmulationProfile profile : ParserEmulationProfile.values()) {
            assertEquals("profile " + profile, profile == ParserEmulationProfile.COMMONMARK_0_31_2,
                    Parser.HTML_COMMENT_ANY_TEXT.get(profile.getProfileOptions()));
        }
    }

    @Test
    public void commonMark0312ProfileKeepsAllCommonMark030Rules() {
        for (com.vladsch.flexmark.util.data.DataKey<Boolean> key : java.util.Arrays.asList(
                Parser.HTML_BLOCK_TEXTAREA_TYPE_1, Parser.REFERENCE_LABEL_UNICODE_CASE_FOLD,
                Parser.HTML_DECLARATION_ASCII_LETTER, Parser.HTML_TAG_WHITESPACE_NO_VT_FF,
                Parser.FENCED_CODE_TILDE_INFO_ALLOWS_BACKTICKS_AND_TILDES,
                Parser.CODE_SPAN_NORMALIZE_LINE_ENDINGS_AND_KEEP_INTERIOR_SPACES)) {
            assertTrue(key.getName(), key.get(ParserEmulationProfile.COMMONMARK_0_31_2.getProfileOptions()));
        }
        assertTrue(ParserEmulationProfile.COMMONMARK_0_31_2.getOptions().isNoItemAtCodeIndent());
    }

    // --- shapes both grammars accept ---

    @Test
    public void ordinaryCommentIsAComment() {
        assertAll("<p>foo <!-- this is a\ncomment - with hyphen --></p>\n", "foo <!-- this is a\ncomment - with hyphen -->\n");
    }

    @Test
    public void emptyCommentIsAComment() {
        assertAll("<p>foo <!----> bar</p>\n", "foo <!----> bar\n");
    }

    @Test
    public void unterminatedCommentIsText() {
        assertAll("<p>foo &lt;!-- bar</p>\n", "foo <!-- bar\n");
        assertAll("<p>foo &lt;!-- bar -- baz --</p>\n", "foo <!-- bar -- baz --\n");
    }

    @Test
    public void commentEndsAtTheFirstClosingSequence() {
        assertAll("<p>a <!-- b --> c --&gt;</p>\n", "a <!-- b --> c -->\n");
    }

    // --- new in 0.31 ---

    @Test
    public void textMayContainDoubleHyphen() {
        assertNew("<p>foo <!-- a--b --> bar</p>\n", "foo <!-- a--b --> bar\n");
        assertNew("<p>foo <!-- a -- b --> bar</p>\n", "foo <!-- a -- b --> bar\n");
        assertNew("<p>foo <!-- a ---- b --> bar</p>\n", "foo <!-- a ---- b --> bar\n");
        assertNew("<p>foo <!-- a\n-- b --> bar</p>\n", "foo <!-- a\n-- b --> bar\n");
    }

    @Test
    public void textMayStartWithHyphens() {
        assertNew("<p>foo <!---- a --> bar</p>\n", "foo <!---- a --> bar\n");
        assertNew("<p>foo <!----a--> bar</p>\n", "foo <!----a--> bar\n");
    }

    @Test
    public void textMayEndWithHyphens() {
        assertNew("<p>foo <!-- a ---> bar</p>\n", "foo <!-- a ---> bar\n");
        assertNew("<p>foo <!-- a ----> bar</p>\n", "foo <!-- a ----> bar\n");
        assertNew("<p>foo <!--a---> bar</p>\n", "foo <!--a---> bar\n");
    }

    @Test
    public void shortEmptyCommentsAreComments() {
        assertNew("<p>foo <!--> bar</p>\n", "foo <!--> bar\n");
        assertNew("<p>foo <!---> bar</p>\n", "foo <!---> bar\n");
    }

    @Test
    public void shortEmptyCommentEndsAtTheFirstClosingSequence() {
        assertNew("<p>a <!---> b --&gt; c</p>\n", "a <!---> b --> c\n");
        assertNew("<p>a <!--> b --&gt; c</p>\n", "a <!--> b --> c\n");
    }

    @Test
    public void markdownInsideACommentIsNotInterpreted() {
        assertNew("<p><em>a</em> <!-- *b* -- _c_ --> <em>d</em></p>\n", "*a* <!-- *b* -- _c_ --> *d*\n");
    }

    @Test
    public void longCommentDoesNotOverflowTheStack() {
        StringBuilder sb = new StringBuilder("x <!--");
        for (int i = 0; i < 20000; i++) sb.append(i % 3 == 0 ? "-a" : "b");
        sb.append("-->");
        assertNew("<p>" + sb + "</p>\n", sb + "\n");
    }

    // --- 0.30 and older keep the old grammar ---

    @Test
    public void oldProfilesRejectDoubleHyphenInText() {
        assertOld("<p>foo &lt;!-- a--b --&gt; bar</p>\n", "foo <!-- a--b --> bar\n");
        assertOld("<p>foo &lt;!---- a --&gt; bar</p>\n", "foo <!---- a --> bar\n");
    }

    @Test
    public void oldProfilesRejectTextEndingInHyphen() {
        assertOld("<p>foo &lt;!-- a ---&gt; bar</p>\n", "foo <!-- a ---> bar\n");
    }

    @Test
    public void oldProfilesRejectShortEmptyComments() {
        assertOld("<p>foo &lt;!--&gt; bar</p>\n", "foo <!--> bar\n");
        assertOld("<p>foo &lt;!---&gt; bar</p>\n", "foo <!---> bar\n");
    }

    @Test
    public void explicitKeyOverridesTheProfile() {
        DataHolder off = new MutableDataSet().set(Parser.HTML_COMMENT_ANY_TEXT, false).toImmutable();
        assertEquals("<p>foo &lt;!--&gt; bar</p>\n",
                HtmlRenderer.builder(off).build().render(Parser.builder(off).build().parse("foo <!--> bar\n")));
        DataHolder on = ParserEmulationProfile.COMMONMARK_0_30.getProfileOptions().toMutable()
                .set(Parser.HTML_COMMENT_ANY_TEXT, true).toImmutable();
        assertEquals("<p>foo <!--> bar</p>\n",
                HtmlRenderer.builder(on).build().render(Parser.builder(on).build().parse("foo <!--> bar\n")));
    }

    // The HTML_TAG pattern is cached by key: parsers with different settings of the flag must not share it
    @Test
    public void patternCacheDoesNotMixConfigurations() {
        for (int i = 0; i < 2; i++) {
            assertEquals("<p>a <!--> b</p>\n", render(ParserEmulationProfile.COMMONMARK_0_31_2, "a <!--> b\n"));
            assertEquals("<p>a &lt;!--&gt; b</p>\n", render(ParserEmulationProfile.COMMONMARK_0_30, "a <!--> b\n"));
            assertEquals("<p>a <!--> b</p>\n", render(null, "a <!--> b\n"));
            assertEquals("<p>a &lt;!--&gt; b</p>\n", render(ParserEmulationProfile.COMMONMARK_0_29, "a <!--> b\n"));
        }
    }

    // --- HTML blocks of type 2 are unchanged: they start with <!-- and end at a line containing --> ---

    @Test
    public void htmlBlockCommentsAreUnchanged() {
        assertAll("<!-- a--b -->\n<p><em>x</em></p>\n", "<!-- a--b -->\n*x*\n");
        assertAll("<!-->\n<p><em>x</em></p>\n", "<!-->\n*x*\n");
        assertAll("<!--->\n<p><em>x</em></p>\n", "<!--->\n*x*\n");
        assertAll("<!-- a\n\n--b\n-->\n<p><em>x</em></p>\n", "<!-- a\n\n--b\n-->\n*x*\n");
    }

    // --- Markdown to Markdown ---

    @Test
    public void formatterKeepsCommentsWithDoubleHyphen() {
        for (String md : new String[] {
                "foo <!-- a--b --> bar\n", "foo <!---- a ----> bar\n", "foo <!--> bar\n", "foo <!---> bar\n",
                "foo <!-- a\n-- b --> bar\n", "<!-- a--b -->\n", "<!-->\n", "<!--->\n"}) {
            for (ParserEmulationProfile profile : NEW_BEHAVIOUR) {
                assertEquals(String.valueOf(profile), md, format(profile, md));
            }
        }
    }

    @Test
    public void formattedMarkdownRendersTheSameHtml() {
        String md = "foo <!-- a--b --> bar <!--> baz <!---> qux <!-- c ---> end\n";
        for (ParserEmulationProfile profile : NEW_BEHAVIOUR) {
            assertEquals(render(profile, md), render(profile, format(profile, md)));
            assertFalse(render(profile, md).contains("&lt;"));
        }
    }
}
