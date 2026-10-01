package com.vladsch.flexmark.core.test.util.parser;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.data.DataHolder;

import static org.junit.Assert.assertEquals;

/**
 * Shared helpers for the tests that pin a CommonMark 0.30 rule against the default behaviour (no options) and against
 * every earlier profile.
 */
final class Profile030Support {
    private static final ParserEmulationProfile[] PRE_030 = {
            ParserEmulationProfile.COMMONMARK_0_26,
            ParserEmulationProfile.COMMONMARK_0_27,
            ParserEmulationProfile.COMMONMARK_0_28,
            ParserEmulationProfile.COMMONMARK_0_29,
    };

    private Profile030Support() {
    }

    static String render(DataHolder options, String markdown) {
        if (options == null) {
            return HtmlRenderer.builder().build().render(Parser.builder().build().parse(markdown));
        }
        return HtmlRenderer.builder(options).build().render(Parser.builder(options).build().parse(markdown));
    }

    // no options at all, the default (0.31.2) keeps the 0.30 behaviour
    static void assert030(String expected, String markdown) {
        assertEquals(expected, render(null, markdown));
    }

    static void assertPre030(String expected, String markdown) {
        for (ParserEmulationProfile profile : PRE_030) {
            assertEquals("profile " + profile, expected, render(profile.getProfileOptions().toImmutable(), markdown));
        }
    }
}