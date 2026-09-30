package com.vladsch.flexmark.core.test.util.parser;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.data.DataHolder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * CommonMark 0.30 (#657): a line starting with &lt;textarea starts an HTML block of type 1, like pre, script and
 * style. Its content is passed through raw and it ends with a line containing &lt;/textarea&gt;, not at a blank line.
 */
public class HtmlBlockTextareaTest {
    private static final ParserEmulationProfile[] PRE_030 = {
            ParserEmulationProfile.COMMONMARK_0_26,
            ParserEmulationProfile.COMMONMARK_0_27,
            ParserEmulationProfile.COMMONMARK_0_28,
            ParserEmulationProfile.COMMONMARK_0_29,
    };

    private static String render(DataHolder options, String markdown) {
        if (options == null) {
            return HtmlRenderer.builder().build().render(Parser.builder().build().parse(markdown));
        }
        return HtmlRenderer.builder(options).build().render(Parser.builder(options).build().parse(markdown));
    }

    // no options at all, 0.30 is the default behaviour
    private static void assert030(String expected, String markdown) {
        assertEquals(expected, render(null, markdown));
    }

    private static void assertPre030(String expected, String markdown) {
        for (ParserEmulationProfile profile : PRE_030) {
            assertEquals("profile " + profile, expected, render(profile.getProfileOptions().toImmutable(), markdown));
        }
    }

    @Test
    public void blankLinesDoNotEndTheBlockIn030() {
        assert030("<textarea>\n\n*foo*\n\n_bar_\n\n</textarea>\n", "<textarea>\n\n*foo*\n\n_bar_\n\n</textarea>\n");
    }

    @Test
    public void blankLinesEndTheTypeSevenBlockBefore030() {
        assertPre030("<textarea>\n<p><em>foo</em></p>\n<p><em>bar</em></p>\n</textarea>\n",
                "<textarea>\n\n*foo*\n\n_bar_\n\n</textarea>\n");
    }

    @Test
    public void startTagIsCaseInsensitive() {
        assert030("<TextArea>\n*foo*\n\n*bar*\n</TEXTAREA>\n", "<TextArea>\n*foo*\n\n*bar*\n</TEXTAREA>\n");
    }

    @Test
    public void startTagMayHaveAttributes() {
        assert030("<textarea rows=3>\n*foo*\n\n*bar*\n</textarea>\n", "<textarea rows=3>\n*foo*\n\n*bar*\n</textarea>\n");
    }

    @Test
    public void startTagMayBeFollowedByATab() {
        assert030("<textarea\t>\n*foo*\n\n*bar*\n</textarea>\n", "<textarea\t>\n*foo*\n\n*bar*\n</textarea>\n");
    }

    @Test
    public void startTagMayBeFollowedByTextOnTheSameLine() {
        assert030("<textarea>*foo*\n\n*bar*\n</textarea>\n", "<textarea>*foo*\n\n*bar*\n</textarea>\n");
    }

    @Test
    public void longerTagNameDoesNotStartTypeOne() {
        assert030("<textareax>\n<p><em>a</em></p>\n", "<textareax>\n\n*a*\n");
        assertPre030("<textareax>\n<p><em>a</em></p>\n", "<textareax>\n\n*a*\n");
    }

    @Test
    public void blockEndingOnItsFirstLineContainsJustThatLine() {
        assert030("<textarea>*a*</textarea>\n<p><em>b</em></p>\n", "<textarea>*a*</textarea>\n*b*\n");
    }

    @Test
    public void textContinuesInAParagraphBefore030() {
        assertPre030("<p><textarea><em>a</em></textarea>\n<em>b</em></p>\n", "<textarea>*a*</textarea>\n*b*\n");
    }

    @Test
    public void endTagOfAnotherTypeOneTagEndsTheBlock() {
        assert030("<textarea>\n*a*\n</pre>\n<p><em>b</em></p>\n", "<textarea>\n*a*\n</pre>\n\n*b*\n");
    }

    @Test
    public void closingTagOfTextareaEndsOtherTypeOneBlocks() {
        assert030("<pre>\n*a*\n</textarea>\n<p><em>b</em></p>\n", "<pre>\n*a*\n</textarea>\n\n*b*\n");
        // before 0.30 only the end of the pre block is an end tag, the rest of the document is raw\n        assertPre030("<pre>\n*a*\n</textarea>\n\n*b*\n", "<pre>\n*a*\n</textarea>\n\n*b*\n");
    }

    @Test
    public void textareaInterruptsAParagraphIn030() {
        assert030("<p>foo</p>\n<textarea>\n*a*\n\n*b*\n</textarea>\n", "foo\n<textarea>\n*a*\n\n*b*\n</textarea>\n");
    }

    @Test
    public void unclosedBlockRunsToTheEndOfTheDocument() {
        assert030("<textarea>\n*a*\n\n*b*\n", "<textarea>\n*a*\n\n*b*\n");
    }

    @Test
    public void textareaInsideABlockQuote() {
        assert030("<blockquote>\n<textarea>\n*a*\n\n*b*\n</textarea>\n</blockquote>\n",
                "> <textarea>\n> *a*\n>\n> *b*\n> </textarea>\n");
    }

    @Test
    public void formatterKeepsTheRawBlockUnchanged() {
        String markdown = "foo\n\n<textarea>\n\n*foo*\n\n_bar_\n\n</textarea>\n\nbaz\n";
        assertEquals(markdown, Formatter.builder().build().render(Parser.builder().build().parse(markdown)));
    }
}