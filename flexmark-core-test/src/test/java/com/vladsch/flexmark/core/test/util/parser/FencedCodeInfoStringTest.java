package com.vladsch.flexmark.core.test.util.parser;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.data.DataHolder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * CommonMark 0.29 (#119, #505): info strings of backtick fences cannot contain backticks, info strings of
 * tilde fences can contain backticks and tildes, and the info string is trimmed of all whitespace.
 */
public class FencedCodeInfoStringTest {
    private static String render(ParserEmulationProfile profile, String markdown) {
        DataHolder options = profile.getProfileOptions().toImmutable();
        return HtmlRenderer.builder(options).build().render(Parser.builder(options).build().parse(markdown));
    }

    private static final String TILDE_FENCE_WITH_BACKTICKS = "~~~ aa ``` ~~~\nfoo\n~~~\n";
    private static final String TILDE_FENCE_WITH_BACKTICKS_HTML = "<pre><code class=\"language-aa\">foo\n</code></pre>\n";
    private static final String TILDE_FENCE_WITH_TILDES_ONLY = "~~~ aa ~~~\nfoo\n~~~\n";

    @Test
    public void tildeFenceInfoMayContainBackticksAndTildesIn029() {
        assertEquals(TILDE_FENCE_WITH_BACKTICKS_HTML, render(ParserEmulationProfile.COMMONMARK_0_29, TILDE_FENCE_WITH_BACKTICKS));
        assertEquals(TILDE_FENCE_WITH_BACKTICKS_HTML, render(ParserEmulationProfile.COMMONMARK_0_29, TILDE_FENCE_WITH_TILDES_ONLY));
    }

    @Test
    public void tildeFenceInfoWithBackticksOrTildesIsNotAFenceBefore029() {
        for (ParserEmulationProfile profile : new ParserEmulationProfile[] {
                ParserEmulationProfile.COMMONMARK_0_26,
                ParserEmulationProfile.COMMONMARK_0_27,
                ParserEmulationProfile.COMMONMARK_0_28,
                ParserEmulationProfile.COMMONMARK_LATEST}) {
            assertEquals("profile " + profile, "<p>~~~ aa ``` ~~~\nfoo</p>\n<pre><code></code></pre>\n",
                    render(profile, TILDE_FENCE_WITH_BACKTICKS));
        }
    }

    @Test
    public void backtickFenceInfoCannotContainBackticksInAnyProfile() {
        for (ParserEmulationProfile profile : new ParserEmulationProfile[] {
                ParserEmulationProfile.COMMONMARK_0_28, ParserEmulationProfile.COMMONMARK_0_29}) {
            assertEquals("profile " + profile, "<p><code>aa</code>\nfoo</p>\n", render(profile, "``` aa ```\nfoo\n"));
        }
    }

    @Test
    public void infoStringIsTrimmedOfTabsIn029() {
        assertEquals("<pre><code class=\"language-aa\">foo\n</code></pre>\n",
                render(ParserEmulationProfile.COMMONMARK_0_29, "```\taa\t \nfoo\n```\n"));
    }
}

