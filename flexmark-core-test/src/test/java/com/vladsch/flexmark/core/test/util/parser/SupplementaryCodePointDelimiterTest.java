package com.vladsch.flexmark.core.test.util.parser;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Emphasis delimiter flanking is defined on Unicode characters (code points), not UTF-16 units. A supplementary
 * character is a surrogate pair in a Java string and must be classified as a whole.
 * <p>
 * No CommonMark spec example covers this, so the expectations are derived from the rules in spec 0.30,
 * "Left-flanking delimiter run" / "Right-flanking delimiter run" and "Emphasis and strong emphasis":
 * <ul>
 * <li>left-flanking: not followed by Unicode whitespace, and either not followed by punctuation, or followed by
 * punctuation and preceded by whitespace or punctuation; right-flanking is the mirror image;</li>
 * <li>{@code *} can open if left-flanking and can close if right-flanking;</li>
 * <li>{@code _} can open if left-flanking and (not right-flanking or preceded by punctuation); it can close if
 * right-flanking and (not left-flanking or followed by punctuation);</li>
 * <li>the beginning and the end of the line count as Unicode whitespace.</li>
 * </ul>
 * Punctuation is any ASCII punctuation or Unicode P* character. U+10100 AEGEAN WORD SEPARATOR LINE is {@code Po}
 * and U+1D400 MATHEMATICAL BOLD CAPITAL A is {@code Lu}; both are supplementary. U+00A1 is the BMP {@code Po}
 * control.
 */
public class SupplementaryCodePointDelimiterTest {
    private static final String BMP_PUNCT = "\u00A1";
    private static final String SUPP_PUNCT = new String(Character.toChars(0x10100));
    private static final String BMP_LETTER = "\u00C0";
    private static final String SUPP_LETTER = new String(Character.toChars(0x1D400));

    private static final ParserEmulationProfile[] PROFILES = {
            null,
            ParserEmulationProfile.COMMONMARK_0_26,
            ParserEmulationProfile.COMMONMARK_0_29,
            ParserEmulationProfile.COMMONMARK_0_30,
    };

    private static String render(DataHolder options, String markdown) {
        return HtmlRenderer.builder(options).build().render(Parser.builder(options).build().parse(markdown));
    }

    private static void assertPara(String expected, String markdown) {
        for (ParserEmulationProfile profile : PROFILES) {
            DataHolder options = profile == null ? new MutableDataSet().toImmutable() : profile.getProfileOptions().toImmutable();
            assertEquals(String.valueOf(profile), "<p>" + expected + "</p>\n", render(options, markdown));
        }
    }

    // `*` before punctuation and after a letter is not left-flanking: it cannot open
    private void starCannotOpenBeforePunctuation(String p) {
        assertPara("a*" + p + "foo*", "a*" + p + "foo*");
    }

    // `*` after punctuation and before a letter is not right-flanking: it cannot close
    private void starCannotCloseAfterPunctuation(String p) {
        assertPara("*foo" + p + "*a", "*foo" + p + "*a");
    }

    // `_` followed by punctuation is only right-flanking: it closes
    private void underscoreClosesBeforePunctuation(String p) {
        assertPara("<em>foo</em>" + p, "_foo_" + p);
    }

    // `_` preceded by punctuation is only left-flanking: it opens
    private void underscoreOpensAfterPunctuation(String p) {
        assertPara(p + "<em>foo</em>", p + "_foo_");
    }

    @Test
    public void bmpStarCannotOpenBeforePunctuation() {
        starCannotOpenBeforePunctuation(BMP_PUNCT);
    }

    @Test
    public void supplementaryStarCannotOpenBeforePunctuation() {
        starCannotOpenBeforePunctuation(SUPP_PUNCT);
    }

    @Test
    public void bmpStarCannotCloseAfterPunctuation() {
        starCannotCloseAfterPunctuation(BMP_PUNCT);
    }

    @Test
    public void supplementaryStarCannotCloseAfterPunctuation() {
        starCannotCloseAfterPunctuation(SUPP_PUNCT);
    }

    @Test
    public void bmpUnderscoreClosesBeforePunctuation() {
        underscoreClosesBeforePunctuation(BMP_PUNCT);
    }

    @Test
    public void supplementaryUnderscoreClosesBeforePunctuation() {
        underscoreClosesBeforePunctuation(SUPP_PUNCT);
    }

    @Test
    public void bmpUnderscoreOpensAfterPunctuation() {
        underscoreOpensAfterPunctuation(BMP_PUNCT);
    }

    @Test
    public void supplementaryUnderscoreOpensAfterPunctuation() {
        underscoreOpensAfterPunctuation(SUPP_PUNCT);
    }

    // a letter is not punctuation: intraword `*` emphasis works, intraword `_` emphasis does not
    private void letterBehavesLikeText(String l) {
        assertPara(l + "<em>foo</em>" + l, l + "*foo*" + l);
        assertPara(l + "_foo_" + l, l + "_foo_" + l);
        assertPara(l + "_foo_", l + "_foo_");
        assertPara("_foo_" + l, "_foo_" + l);
        assertPara("<em>" + l + "</em>", "*" + l + "*");
        assertPara("<em>" + l + "</em>", "_" + l + "_");
    }

    @Test
    public void bmpLetterBehavesLikeText() {
        letterBehavesLikeText(BMP_LETTER);
    }

    @Test
    public void supplementaryLetterBehavesLikeText() {
        letterBehavesLikeText(SUPP_LETTER);
    }

    // a run at the very start or end of the input must not read outside of it
    private void runsAtInputBounds(String c) {
        assertPara("_" + c, "_" + c);
        assertPara(c + "_", c + "_");
        assertPara("*" + c, "*" + c);
        assertPara(c + "*", c + "*");
        assertPara("<em>" + c + "</em>", "*" + c + "*");
        assertPara("<em>foo" + c + "</em>", "_foo" + c + "_");
        assertPara("<em>" + c + "foo</em>", "_" + c + "foo_");
    }

    @Test
    public void bmpRunsAtInputBounds() {
        runsAtInputBounds(BMP_PUNCT);
        runsAtInputBounds(BMP_LETTER);
    }

    @Test
    public void supplementaryRunsAtInputBounds() {
        runsAtInputBounds(SUPP_PUNCT);
        runsAtInputBounds(SUPP_LETTER);
    }
}
