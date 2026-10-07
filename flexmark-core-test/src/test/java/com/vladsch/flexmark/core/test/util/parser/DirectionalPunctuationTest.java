package com.vladsch.flexmark.core.test.util.parser;

import com.vladsch.flexmark.ast.util.Parsing;
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
 * {@link Parser#INLINE_DELIMITER_DIRECTIONAL_PUNCTUATIONS} (a flexmark extension, not part of CommonMark, default
 * off) classifies the character before a delimiter run with an 'opening' punctuation set and the character after it
 * with a 'closing' one. The sets are: ASCII punctuation, plus the Unicode categories P (and S unless
 * {@link Parser#UNICODE_PUNCTUATION_INCLUDES_SYMBOLS} is off), with {@code Ps} only in the opening and {@code Pe}
 * only in the closing set. {@code Pi} and {@code Pf} are locale dependent ({@code \u00BB} closes in French and
 * opens in German) so they are in both sets, as in the plain punctuation set.
 * <p>
 * Expected values follow from the rules: with the option on, {@code beforeIsPunctuation} means 'in the opening set',
 * {@code afterIsPunctuation} means 'in the closing set', the rest of the flanking rules are those of the spec.
 */
public class DirectionalPunctuationTest {
    private static final String FW_LPAREN = "\uFF08";
    private static final String FW_RPAREN = "\uFF09";
    private static final String FW_LBRACKET = "\uFF3B";
    private static final String FW_RBRACKET = "\uFF3D";
    private static final String LAQUO = "\u00AB";
    private static final String RAQUO = "\u00BB";
    private static final String EM_DASH = "\u2014";
    private static final String IDEOGRAPHIC_STOP = "\u3002";
    private static final String COPYRIGHT = "\u00A9";
    private static final String EURO = "\u20AC";
    private static final String INVERTED_QUESTION = "\u00BF";
    private static final String CJK = "\u53EF\u4EE5";

    private static final DataHolder NO_SYMBOLS_ON = new MutableDataSet()
            .set(Parser.INLINE_DELIMITER_DIRECTIONAL_PUNCTUATIONS, true)
            .set(Parser.UNICODE_PUNCTUATION_INCLUDES_SYMBOLS, false)
            .toImmutable();

    private static final DataHolder ON = new MutableDataSet()
            .set(Parser.INLINE_DELIMITER_DIRECTIONAL_PUNCTUATIONS, true)
            .toImmutable();

    private static final DataHolder OFF = new MutableDataSet().toImmutable();

    private static final ParserEmulationProfile[] PROFILES = {
            ParserEmulationProfile.COMMONMARK_0_26,
            ParserEmulationProfile.COMMONMARK_0_30,
            ParserEmulationProfile.COMMONMARK_0_31_2,
    };

    private static String render(DataHolder options, String markdown) {
        return HtmlRenderer.builder(options).build().render(Parser.builder(options).build().parse(markdown));
    }

    private static String p(String html) {
        return "<p>" + html + "</p>\n";
    }

    private static void assertRenders(DataHolder options, String expectedHtml, String markdown) {
        assertEquals(markdown, p(expectedHtml), render(options, markdown));
    }

    private static DataHolder withProfile(ParserEmulationProfile profile, boolean directional) {
        return new MutableDataSet(profile.getProfileOptions())
                .set(Parser.INLINE_DELIMITER_DIRECTIONAL_PUNCTUATIONS, directional)
                .toImmutable();
    }

    // --- the asymmetry: an opening bracket is no closing punctuation and vice versa ---

    @Test
    public void closingBracketAfterDelimiterIsClosingPunctuation() {
        // the first * is followed by closing punctuation and preceded by a letter: not left-flanking, cannot open
        assertRenders(ON, "a*" + FW_RPAREN + "b*", "a*" + FW_RPAREN + "b*");
        assertRenders(ON, "a*" + FW_RBRACKET + "b*", "a*" + FW_RBRACKET + "b*");
        // an opening bracket is not closing punctuation, the delimiter opens
        assertRenders(ON, "a<em>" + FW_LPAREN + "b</em>", "a*" + FW_LPAREN + "b*");
        assertRenders(ON, "a<em>" + FW_LBRACKET + "b</em>", "a*" + FW_LBRACKET + "b*");
    }

    @Test
    public void openingBracketBeforeDelimiterIsOpeningPunctuation() {
        // the second * is preceded by opening punctuation and followed by a letter: not right-flanking, cannot close
        assertRenders(ON, "*b" + FW_LPAREN + "*c", "*b" + FW_LPAREN + "*c");
        assertRenders(ON, "*b" + FW_LBRACKET + "*c", "*b" + FW_LBRACKET + "*c");
        // a closing bracket is not opening punctuation, the delimiter closes
        assertRenders(ON, "<em>b" + FW_RPAREN + "</em>c", "*b" + FW_RPAREN + "*c");
        assertRenders(ON, "<em>b" + FW_RBRACKET + "</em>c", "*b" + FW_RBRACKET + "*c");
    }

    // --- characters in both sets ---

    @Test
    public void guillemetsAreInBothSets() {
        assertRenders(ON, "a" + LAQUO + "<em>b</em>" + RAQUO + "c", "a" + LAQUO + "_b_" + RAQUO + "c");
        assertRenders(ON, "a" + RAQUO + "<em>b</em>" + LAQUO + "c", "a" + RAQUO + "_b_" + LAQUO + "c");
    }

    @Test
    public void emDashAndIdeographicStopAreInBothSets() {
        assertRenders(ON, "a" + EM_DASH + "<em>b</em>" + EM_DASH + "c", "a" + EM_DASH + "_b_" + EM_DASH + "c");
        assertRenders(ON, "a" + IDEOGRAPHIC_STOP + "<em>b</em>" + IDEOGRAPHIC_STOP + "c",
                "a" + IDEOGRAPHIC_STOP + "_b_" + IDEOGRAPHIC_STOP + "c");
    }

    @Test
    public void symbolsAreInBothSetsUnlessTheOptionIsOff() {
        for (String s : new String[]{COPYRIGHT, EURO}) {
            String md = "a" + s + "_b_" + s + "c";
            assertRenders(ON, "a" + s + "<em>b</em>" + s + "c", md);
            // a symbol that is no punctuation is a letter: both-flanking _ inside a word cannot open nor close
            assertRenders(NO_SYMBOLS_ON, "a" + s + "_b_" + s + "c", md);
        }
    }

    // --- damage measured before the fix: the option must not make Unicode punctuation disappear ---

    @Test
    public void unicodePunctuationStillClassifiedWithOptionOn() {
        assertRenders(ON, "a" + LAQUO + "<em>b</em>" + RAQUO + "c", "a" + LAQUO + "_b_" + RAQUO + "c");
        assertRenders(ON, "a" + EM_DASH + "<em>b</em>" + EM_DASH + "c", "a" + EM_DASH + "_b_" + EM_DASH + "c");
        assertRenders(ON, "a" + IDEOGRAPHIC_STOP + "<em>b</em>" + IDEOGRAPHIC_STOP + "c",
                "a" + IDEOGRAPHIC_STOP + "_b_" + IDEOGRAPHIC_STOP + "c");
        assertRenders(ON, "a" + EURO + "<em>b</em>" + EURO + "c", "a" + EURO + "_b_" + EURO + "c");
        assertRenders(ON, INVERTED_QUESTION + "<em>b</em>?", INVERTED_QUESTION + "_b_?");
    }

    // --- the CJK use case that justifies the option ---

    @Test
    public void cjkEmphasisWithEmptyParentheses() {
        assertRenders(ON, CJK + "<strong>foo()</strong>" + CJK, CJK + "**foo()**" + CJK);
        assertRenders(ON, CJK + "<strong>()foo()</strong>" + CJK, CJK + "**()foo()**" + CJK);
        assertRenders(ON, CJK + "<strong>()foo</strong>" + CJK, CJK + "**()foo**" + CJK);
        assertRenders(ON, CJK + "**foo(**" + CJK, CJK + "**foo(**" + CJK);
        assertRenders(ON, CJK + "**)foo(**" + CJK, CJK + "**)foo(**" + CJK);
        assertRenders(ON, CJK + "**)foo**" + CJK, CJK + "**)foo**" + CJK);
    }

    // --- control: the spec default is untouched ---

    @Test
    public void optionOffIsTheSpecBehaviour() {
        assertRenders(OFF, "a*" + FW_RPAREN + "b*", "a*" + FW_RPAREN + "b*");
        assertRenders(OFF, "a*" + FW_LPAREN + "b*", "a*" + FW_LPAREN + "b*");
        assertRenders(OFF, CJK + "**foo()**" + CJK, CJK + "**foo()**" + CJK);
        assertRenders(OFF, INVERTED_QUESTION + "<em>b</em>?", INVERTED_QUESTION + "_b_?");
        assertRenders(OFF, "a" + LAQUO + "<em>b</em>" + RAQUO + "c", "a" + LAQUO + "_b_" + RAQUO + "c");
    }

    @Test
    public void everyProfileRendersSymmetricPunctuationLikeTheOptionOff() {
        // punctuation that is in both sets gives the same flanking as the plain set
        String[] inputs = {
                "a" + LAQUO + "_b_" + RAQUO + "c", "a" + EM_DASH + "_b_" + EM_DASH + "c",
                "a" + IDEOGRAPHIC_STOP + "_b_" + IDEOGRAPHIC_STOP + "c", "a" + EURO + "_b_" + EURO + "c",
                "a" + COPYRIGHT + "_b_" + COPYRIGHT + "c", INVERTED_QUESTION + "_b_?",
        };
        for (ParserEmulationProfile profile : PROFILES) {
            for (String md : inputs) {
                assertEquals(profile + " " + md, render(withProfile(profile, false), md), render(withProfile(profile, true), md));
            }
        }
    }

    // --- the patterns themselves, every code point ---

    private static boolean isPunctuation(int c, boolean symbols) {
        switch (Character.getType(c)) {
            case Character.CONNECTOR_PUNCTUATION:
            case Character.DASH_PUNCTUATION:
            case Character.START_PUNCTUATION:
            case Character.END_PUNCTUATION:
            case Character.INITIAL_QUOTE_PUNCTUATION:
            case Character.FINAL_QUOTE_PUNCTUATION:
            case Character.OTHER_PUNCTUATION:
                return true;
            case Character.CURRENCY_SYMBOL:
            case Character.MODIFIER_SYMBOL:
            case Character.MATH_SYMBOL:
            case Character.OTHER_SYMBOL:
                return symbols;
            default:
                return false;
        }
    }

    private static void sweep(boolean symbols) {
        DataHolder options = new MutableDataSet()
                .set(Parser.UNICODE_PUNCTUATION_INCLUDES_SYMBOLS, symbols)
                .toImmutable();
        Parsing parsing = new Parsing(options);

        for (int c = 0; c <= Character.MAX_CODE_POINT; c++) {
            if (c >= 0xD800 && c <= 0xDFFF) continue;
            String s = new String(Character.toChars(c));
            boolean open;
            boolean close;

            if (c < 128) {
                open = parsing.ASCII_PUNCTUATION.indexOf(c) >= 0 || parsing.ASCII_OPEN_PUNCTUATION.replace("\\", "").indexOf(c) >= 0;
                close = parsing.ASCII_PUNCTUATION.indexOf(c) >= 0 || parsing.ASCII_CLOSE_PUNCTUATION.replace("\\", "").indexOf(c) >= 0;
                // the ASCII sets contain backslash escapes, a backslash itself is ASCII punctuation
                if (c == '\\') open = close = true;
            } else {
                boolean punctuation = isPunctuation(c, symbols);
                int type = Character.getType(c);
                open = punctuation && type != Character.END_PUNCTUATION;
                close = punctuation && type != Character.START_PUNCTUATION;
            }

            String name = "U+" + Integer.toHexString(c) + " symbols=" + symbols;
            assertEquals(name + " open", open, parsing.PUNCTUATION_OPEN.matcher(s).matches());
            assertEquals(name + " close", close, parsing.PUNCTUATION_CLOSE.matcher(s).matches());
        }
    }

    @Test
    public void openAndCloseSetsOfEveryCodePointWithSymbols() {
        sweep(true);
    }

    @Test
    public void openAndCloseSetsOfEveryCodePointWithoutSymbols() {
        sweep(false);
    }

    @Test
    public void openAndCloseSetsDifferOnlyInBrackets() {
        Parsing parsing = new Parsing(new MutableDataSet().toImmutable());
        assertTrue(parsing.PUNCTUATION_OPEN.matcher(FW_LPAREN).matches());
        assertFalse(parsing.PUNCTUATION_CLOSE.matcher(FW_LPAREN).matches());
        assertFalse(parsing.PUNCTUATION_OPEN.matcher(FW_RPAREN).matches());
        assertTrue(parsing.PUNCTUATION_CLOSE.matcher(FW_RPAREN).matches());
        assertTrue(parsing.PUNCTUATION_OPEN.matcher(LAQUO).matches());
        assertTrue(parsing.PUNCTUATION_CLOSE.matcher(LAQUO).matches());
        assertTrue(parsing.PUNCTUATION_OPEN.matcher(RAQUO).matches());
        assertTrue(parsing.PUNCTUATION_CLOSE.matcher(RAQUO).matches());
    }

    // --- formatter round trip ---

    @Test
    public void formatterRoundTripKeepsTheRenderedOutput() {
        String[] inputs = {
                "a*" + FW_LPAREN + "b*", "*b" + FW_RPAREN + "*c", "a" + LAQUO + "_b_" + RAQUO + "c",
                CJK + "**foo()**" + CJK, "a" + EURO + "_b_" + EURO + "c",
        };
        for (String md : inputs) {
            String formatted = Formatter.builder(ON).build().render(Parser.builder(ON).build().parse(md));
            assertEquals(md, render(ON, md), render(ON, formatted));
        }
    }
}
