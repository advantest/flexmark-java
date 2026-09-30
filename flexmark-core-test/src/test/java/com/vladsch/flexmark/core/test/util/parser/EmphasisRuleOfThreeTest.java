package com.vladsch.flexmark.core.test.util.parser;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.data.DataHolder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * CommonMark 0.29 (#528): the "multiple of 3" rule for delimiter runs that can both open and close does not apply
 * if the lengths of both runs are multiples of 3.
 */
public class EmphasisRuleOfThreeTest {
    private static final ParserEmulationProfile[] PRE_029 = {
            ParserEmulationProfile.COMMONMARK_0_26,
            ParserEmulationProfile.COMMONMARK_0_27,
            ParserEmulationProfile.COMMONMARK_0_28,
    };

    private static String render(ParserEmulationProfile profile, String markdown) {
        DataHolder options = profile.getProfileOptions().toImmutable();
        return HtmlRenderer.builder(options).build().render(Parser.builder(options).build().parse(markdown));
    }

    private static void assert029(String expected, String markdown) {
        assertEquals(expected, render(ParserEmulationProfile.COMMONMARK_0_29, markdown));
    }

    private static void assertPre029(String expected, String markdown) {
        for (ParserEmulationProfile profile : PRE_029) {
            if (profile == ParserEmulationProfile.COMMONMARK_0_26) {
                continue; // 0.26 has no multiple of 3 rule, covered by the spec tests
            }
            assertEquals("profile " + profile, expected, render(profile, markdown));
        }
    }

    private static void assertAll(String expected, String markdown) {
        assert029(expected, markdown);
        assertPre029(expected, markdown);
    }

    @Test
    public void bothRunsOfThreeMatchIn029() {
        assert029("<p>foo<em><strong>bar</strong></em>baz</p>\n", "foo***bar***baz\n");
    }

    @Test
    public void bothRunsOfThreeDoNotMatchBefore029() {
        assertPre029("<p>foo***bar***baz</p>\n", "foo***bar***baz\n");
    }

    @Test
    public void longRunsWhichAreMultiplesOfThreeMatchIn029() {
        assert029("<p>foo<strong><strong><strong>bar</strong></strong></strong>***baz</p>\n",
                "foo******bar*********baz\n");
    }

    @Test
    public void longRunsWhichAreMultiplesOfThreeDoNotMatchBefore029() {
        assertPre029("<p>foo******bar*********baz</p>\n", "foo******bar*********baz\n");
    }

    // the rule uses the original length of a run, not what is left of it after a partial match
    @Test
    public void ruleUsesOriginalRunLengthIn029() {
        assert029("<p><strong>a</strong><em>b</em><em>c</em>*</p>\n", "**a***b**c**\n");
    }

    // the cases which motivated the original rule keep working in every version
    @Test
    public void originalMotivatingCases() {
        assertAll("<p><em>foo<strong>bar</strong>baz</em></p>\n", "*foo**bar**baz*\n");
        assertAll("<p><em>foo**bar</em></p>\n", "*foo**bar*\n");
        assertAll("<p><em><strong>foo</strong> bar</em></p>\n", "***foo** bar*\n");
        assertAll("<p><em>foo <strong>bar</strong></em></p>\n", "*foo **bar***\n");
    }
}
