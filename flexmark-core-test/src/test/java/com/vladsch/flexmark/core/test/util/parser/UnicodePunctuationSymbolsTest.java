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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * CommonMark 0.31 added the Unicode symbol categories to the definition of a Unicode punctuation character:
 * "a character in the Unicode {@code P} (punctuation) or {@code S} (symbol) general categories" (0.30:
 * ASCII punctuation or {@code Pc, Pd, Pe, Pf, Pi, Po, Ps}). Punctuation governs the left and right flanking
 * delimiter run rules. Spec example 354 covers only the BMP currency symbols {@code $ £ €}, the rest is tested here:
 * {@code Sc}, {@code Sm}, {@code Sk}, {@code So}, supplementary (non-BMP) characters of each of them, and
 * an exhaustive sweep over every non-ASCII {@code S} and {@code P} code point.
 * <p>
 * All expectations are derived from the spec rules, not recorded from the output. The rules used:
 * <ul>
 * <li>left-flanking: not followed by Unicode whitespace, and either not followed by punctuation, or followed by
 * punctuation and preceded by whitespace or punctuation; right-flanking is the mirror image;</li>
 * <li>{@code *} can open if left-flanking and can close if right-flanking;</li>
 * <li>{@code _} can open if left-flanking and (not right-flanking or preceded by punctuation); it can close if
 * right-flanking and (not left-flanking or followed by punctuation);</li>
 * <li>the beginning and the end of the line count as Unicode whitespace.</li>
 * </ul>
 * Five scenarios, each with the symbol {@code S}, tell a symbol that is punctuation from a letter. A letter is
 * the control: under every configuration it is not punctuation and behaves the 'old' way.
 * <ul>
 * <li>A {@code a*Sfoo*}: the first {@code *} is followed by punctuation and preceded by a letter, so it is
 * not left-flanking and cannot open. With a letter it opens.</li>
 * <li>B {@code *fooS*a}: the second {@code *} is preceded by punctuation and followed by a letter, so it
 * is not right-flanking and cannot close. With a letter it closes.</li>
 * <li>C {@code *S*alpha.}: the form of spec example 354, same reason as B.</li>
 * <li>D {@code _foo_S}: the {@code _} is only right-flanking when followed by punctuation, so it closes.
 * With a letter it is both-flanking and followed by a letter, so it cannot close.</li>
 * <li>E {@code S_foo_}: the {@code _} is only left-flanking when preceded by punctuation, so it opens.
 * With a letter it is both-flanking and preceded by a letter, so it cannot open.</li>
 * </ul>
 * Scenarios A and B put the symbol on each side of a delimiter run and C, D and E use the other side or
 * the line boundary, so for a supplementary symbol both {@code codePointBefore} and {@code codePointAt} are exercised.
 * <p>
 * The new rule is the default and the rule of {@link ParserEmulationProfile#COMMONMARK_0_31_2}. Every other
 * profile, 0.26 to 0.30 are tested, keeps the old rule through {@link Parser#UNICODE_PUNCTUATION_INCLUDES_SYMBOLS}.
 * <p>
 * {@link Parser#INLINE_DELIMITER_DIRECTIONAL_PUNCTUATIONS} (a flexmark extension, default off) is covered by
 * {@link DirectionalPunctuationTest}. Here only a smoke test checks that symbols and letters survive it.
 */
public class UnicodePunctuationSymbolsTest {
    private static final DataHolder DEFAULT = new MutableDataSet().toImmutable();

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

    private static String cp(int codePoint) {
        return new String(Character.toChars(codePoint));
    }

    // Sc
    private static final String YEN = "\u00A5";
    private static final String CENT = "\u00A2";
    private static final String POUND = "\u00A3";
    private static final String EURO = "\u20AC";
    private static final String RUPEE = "\u20B9";
    // Sm
    private static final String PLUS_MINUS = "\u00B1";
    private static final String TIMES = "\u00D7";
    private static final String DIVISION = "\u00F7";
    private static final String N_ARY_SUMMATION = "\u2211";
    // Sk
    private static final String DIAERESIS = "\u00A8";
    private static final String UP_ARROWHEAD = "\u02C4";
    private static final String ACUTE = "\u00B4";
    private static final String MACRON = "\u00AF";
    // So
    private static final String COPYRIGHT = "\u00A9";
    private static final String REGISTERED = "\u00AE";
    private static final String DEGREE = "\u00B0";
    private static final String BLACK_HEART = "\u2665";
    private static final String SNOWMAN = "\u2603";
    // supplementary, one of each category
    private static final String SUPP_SC = cp(0x1E2FF);  // WANCHO NGUN SIGN
    private static final String SUPP_SM = cp(0x1D6C1);  // MATHEMATICAL BOLD NABLA
    private static final String SUPP_SK = cp(0x1F3FB);  // EMOJI MODIFIER FITZPATRICK TYPE-1-2
    private static final String SUPP_SO = cp(0x1F600);  // GRINNING FACE
    // negative controls
    private static final String BMP_LETTER = "\u00C0";
    private static final String SUPP_LETTER = cp(0x1D400); // MATHEMATICAL BOLD CAPITAL A, Lu
    private static final String ASCII_LETTER = "x";
    private static final String SUPP_PUNCT = cp(0x10100); // AEGEAN WORD SEPARATOR LINE, Po
    private static final String BMP_PUNCT = "\u00A1"; // Po

    private static final String[] SYMBOLS = {
            YEN, CENT, POUND, EURO, RUPEE,
            PLUS_MINUS, TIMES, DIVISION, N_ARY_SUMMATION,
            DIAERESIS, UP_ARROWHEAD, ACUTE, MACRON,
            COPYRIGHT, REGISTERED, DEGREE, BLACK_HEART, SNOWMAN,
            SUPP_SC, SUPP_SM, SUPP_SK, SUPP_SO,
    };

    private static DataHolder options(ParserEmulationProfile profile) {
        return profile == null ? DEFAULT : profile.getProfileOptions().toImmutable();
    }

    private static String render(DataHolder options, String markdown) {
        return HtmlRenderer.builder(options).build().render(Parser.builder(options).build().parse(markdown));
    }

    private static String format(DataHolder options, String markdown) {
        return Formatter.builder(options).build().render(Parser.builder(options).build().parse(markdown));
    }

    private static void assertNew(String expected, String markdown) {
        for (ParserEmulationProfile profile : NEW_BEHAVIOUR) {
            assertEquals(String.valueOf(profile) + " " + markdown, expected, render(options(profile), markdown));
        }
    }

    private static void assertOld(String expected, String markdown) {
        for (ParserEmulationProfile profile : OLD_BEHAVIOUR) {
            assertEquals(String.valueOf(profile) + " " + markdown, expected, render(options(profile), markdown));
        }
    }

    private static void assertAll(String expected, String markdown) {
        assertNew(expected, markdown);
        assertOld(expected, markdown);
    }

    private static String p(String html) {
        return "<p>" + html + "</p>\n";
    }

    // A: `*` before punctuation and after a letter is not left-flanking: it cannot open
    private static void scenarioA(String s, boolean punctuation) {
        String markdown = "a*" + s + "foo*";
        String literal = p("a*" + s + "foo*");
        String emphasis = p("a<em>" + s + "foo</em>");
        assertNew(punctuation ? literal : emphasis, markdown);
        assertOld(emphasis, markdown);
    }

    // B: `*` after punctuation and before a letter is not right-flanking: it cannot close
    private static void scenarioB(String s, boolean punctuation) {
        String markdown = "*foo" + s + "*a";
        String literal = p("*foo" + s + "*a");
        String emphasis = p("<em>foo" + s + "</em>a");
        assertNew(punctuation ? literal : emphasis, markdown);
        assertOld(emphasis, markdown);
    }

    // C: the form of spec example 354
    private static void scenarioC(String s, boolean punctuation) {
        String markdown = "*" + s + "*alpha.";
        String literal = p("*" + s + "*alpha.");
        String emphasis = p("<em>" + s + "</em>alpha.");
        assertNew(punctuation ? literal : emphasis, markdown);
        assertOld(emphasis, markdown);
    }

    // D: `_` followed by punctuation is right-flanking only: it closes
    private static void scenarioD(String s, boolean punctuation) {
        String markdown = "_foo_" + s;
        String closes = p("<em>foo</em>" + s);
        String literal = p("_foo_" + s);
        assertNew(punctuation ? closes : literal, markdown);
        assertOld(literal, markdown);
    }

    // E: `_` preceded by punctuation is left-flanking only: it opens
    private static void scenarioE(String s, boolean punctuation) {
        String markdown = s + "_foo_";
        String opens = p(s + "<em>foo</em>");
        String literal = p(s + "_foo_");
        assertNew(punctuation ? opens : literal, markdown);
        assertOld(literal, markdown);
    }

    private static void scenarios(String s, boolean newPunctuation) {
        scenarioA(s, newPunctuation);
        scenarioB(s, newPunctuation);
        scenarioC(s, newPunctuation);
        scenarioD(s, newPunctuation);
        scenarioE(s, newPunctuation);
    }

    // `*` after whitespace and before punctuation is left-flanking in both versions: this is no discriminator,
    // it must not change
    private static void unchangedScenarios(String s) {
        assertAll(p("x <em>" + s + "foo</em>"), "x *" + s + "foo*");
        assertAll(p(s + "<em>" + s + "foo</em>"), s + "*" + s + "foo*");
        assertAll(p("<em>foo" + s + "</em> x"), "*foo" + s + "* x");
    }

    @Test
    public void keyIsOnByDefaultAndOnlyKeptByTheCommonMark0312Profile() {
        assertTrue(Parser.UNICODE_PUNCTUATION_INCLUDES_SYMBOLS.get(null));
        for (ParserEmulationProfile profile : ParserEmulationProfile.values()) {
            assertEquals("profile " + profile, profile == ParserEmulationProfile.COMMONMARK_0_31_2,
                    Parser.UNICODE_PUNCTUATION_INCLUDES_SYMBOLS.get(profile.getProfileOptions()));
        }
    }

    @Test
    public void spec354FormWithTheSpecSymbols() {
        assertNew(p("*$*alpha.") + p("*" + POUND + "*alpha.") + p("*" + EURO + "*alpha."),
                "*$*alpha.\n\n*" + POUND + "*alpha.\n\n*" + EURO + "*alpha.\n");
        // $ is ASCII punctuation, so it is punctuation before 0.31 as well
        assertOld(p("*$*alpha.") + p("<em>" + POUND + "</em>alpha.") + p("<em>" + EURO + "</em>alpha."),
                "*$*alpha.\n\n*" + POUND + "*alpha.\n\n*" + EURO + "*alpha.\n");
    }

    // $ is ASCII punctuation, so in 0.30 as well as in 0.31 the spec 354 form keeps `$` as punctuation:
    // check scenario C for it, it must not be gated
    @Test
    public void asciiSymbolsAreAlwaysPunctuation() {
        for (String s : new String[]{"$", "+", "<", "=", ">", "^", "`", "|", "~"}) {
            if (s.equals("<") || s.equals(">") || s.equals("`")) {
                continue; // not literal text in the HTML output
            }
            assertAll(p("a*" + s + "foo*"), "a*" + s + "foo*");
            assertAll(p("<em>foo</em>" + s), "_foo_" + s);
        }
    }

    // --- Sc ---

    @Test
    public void currencySymbolsYenCentRupee() {
        for (String s : new String[]{YEN, CENT, RUPEE}) {
            scenarios(s, true);
        }
    }

    @Test
    public void currencySymbolsPoundEuro() {
        for (String s : new String[]{POUND, EURO}) {
            scenarios(s, true);
        }
    }

    // --- Sm ---

    @Test
    public void mathSymbols() {
        for (String s : new String[]{PLUS_MINUS, TIMES, DIVISION, N_ARY_SUMMATION}) {
            scenarios(s, true);
        }
    }

    // --- Sk ---

    @Test
    public void modifierSymbols() {
        for (String s : new String[]{DIAERESIS, UP_ARROWHEAD, ACUTE, MACRON}) {
            scenarios(s, true);
        }
    }

    // --- So ---

    @Test
    public void otherSymbols() {
        for (String s : new String[]{COPYRIGHT, REGISTERED, DEGREE, BLACK_HEART, SNOWMAN}) {
            scenarios(s, true);
        }
    }

    // --- supplementary ---

    @Test
    public void supplementaryCurrencySymbol() {
        scenarios(SUPP_SC, true);
    }

    @Test
    public void supplementaryMathSymbol() {
        scenarios(SUPP_SM, true);
    }

    @Test
    public void supplementaryModifierSymbol() {
        scenarios(SUPP_SK, true);
    }

    @Test
    public void supplementaryOtherSymbolEmoji() {
        scenarios(SUPP_SO, true);
    }

    // a supplementary symbol on both sides of a run, and next to a supplementary letter
    @Test
    public void supplementarySymbolOnBothSidesOfARun() {
        // preceded and followed by punctuation: left and right flanking, both `*` and `_` can open and close
        String both = SUPP_SO + "*foo*" + SUPP_SM;
        assertNew(p(SUPP_SO + "<em>foo</em>" + SUPP_SM), both);
        // letters in the same place: `*` still works intraword
        assertOld(p(SUPP_SO + "<em>foo</em>" + SUPP_SM), both);
        String underscore = SUPP_SO + "_foo_" + SUPP_SM;
        assertNew(p(SUPP_SO + "<em>foo</em>" + SUPP_SM), underscore);
        assertOld(p(SUPP_SO + "_foo_" + SUPP_SM), underscore);
    }

    @Test
    public void supplementarySymbolBesideSupplementaryLetter() {
        // letter, `*`, symbol, letter: the symbol is punctuation, the `*` is not left-flanking (preceded by a letter)
        assertNew(p(SUPP_LETTER + "*" + SUPP_SO + SUPP_LETTER + "*"), SUPP_LETTER + "*" + SUPP_SO + SUPP_LETTER + "*");
        assertOld(p(SUPP_LETTER + "<em>" + SUPP_SO + SUPP_LETTER + "</em>"), SUPP_LETTER + "*" + SUPP_SO + SUPP_LETTER + "*");
    }

    // --- negative controls: not punctuation under either version ---

    @Test
    public void lettersBehaveTheSameInAllVersions() {
        for (String l : new String[]{ASCII_LETTER, BMP_LETTER, SUPP_LETTER}) {
            assertAll(p("a<em>" + l + "foo</em>"), "a*" + l + "foo*");
            assertAll(p("<em>foo" + l + "</em>a"), "*foo" + l + "*a");
            assertAll(p("<em>" + l + "</em>alpha."), "*" + l + "*alpha.");
            assertAll(p("_foo_" + l), "_foo_" + l);
            assertAll(p(l + "_foo_"), l + "_foo_");
        }
    }

    // real punctuation (Po) classifies the same in both versions
    @Test
    public void punctuationIsPunctuationInAllVersions() {
        for (String s : new String[]{BMP_PUNCT, SUPP_PUNCT}) {
            assertAll(p("a*" + s + "foo*"), "a*" + s + "foo*");
            assertAll(p("*foo" + s + "*a"), "*foo" + s + "*a");
            assertAll(p("<em>foo</em>" + s), "_foo_" + s);
            assertAll(p(s + "<em>foo</em>"), s + "_foo_");
        }
    }

    @Test
    public void unaffectedScenariosDoNotChange() {
        for (String s : SYMBOLS) {
            unchangedScenarios(s);
        }
    }

    // --- all symbols, one by one, so a failure names the character ---

    @Test
    public void everyRepresentativeSymbol() {
        for (String s : SYMBOLS) {
            int c = s.codePointAt(0);
            assertTrue("fixture " + Integer.toHexString(c) + " is a symbol", isSymbol(c));
            scenarios(s, true);
        }
    }

    private static boolean isSymbol(int c) {
        int type = Character.getType(c);
        return type == Character.CURRENCY_SYMBOL || type == Character.MODIFIER_SYMBOL
                || type == Character.MATH_SYMBOL || type == Character.OTHER_SYMBOL;
    }

    private static boolean isPunctuationCategory(int c) {
        switch (Character.getType(c)) {
            case Character.CONNECTOR_PUNCTUATION:
            case Character.DASH_PUNCTUATION:
            case Character.END_PUNCTUATION:
            case Character.FINAL_QUOTE_PUNCTUATION:
            case Character.INITIAL_QUOTE_PUNCTUATION:
            case Character.OTHER_PUNCTUATION:
            case Character.START_PUNCTUATION:
                return true;
            default:
                return false;
        }
    }

    /**
     * Pins the Unicode general category of every fixture, so that a JDK upgrade which recategorises one of them fails
     * here, where the cause is obvious, instead of somewhere in the expectations below.
     * <p>
     * Deliberately no {@code UnicodeData.txt} is bundled for the categories, unlike the {@code CaseFolding.txt} bundled
     * for {@link com.vladsch.flexmark.util.sequence.Escaping#caseFold(CharSequence)}. The CommonMark specification
     * defines Unicode punctuation by its general category, and {@link Character#getType(int)} <em>is</em> that
     * property, so the JDK is the authority. Case folding is a different matter: the JDK only offers locale dependent
     * simple casing, never the full folding the specification asks for, which is why that data has to be bundled.
     * <p>
     * The consequence is that the categories follow the Unicode version of the running JDK, currently Unicode 15 on
     * JDK 21. That is intended. The sweep thresholds below are lower bounds, so code points added by a later Unicode
     * version can only loosen them, never break them.
     */
    @Test
    public void fixturesHaveTheCategoryTheyAreMeantToHave() {
        assertEquals(Character.CURRENCY_SYMBOL, Character.getType(YEN.codePointAt(0)));
        assertEquals(Character.CURRENCY_SYMBOL, Character.getType(CENT.codePointAt(0)));
        assertEquals(Character.CURRENCY_SYMBOL, Character.getType(RUPEE.codePointAt(0)));
        assertEquals(Character.CURRENCY_SYMBOL, Character.getType(SUPP_SC.codePointAt(0)));
        assertEquals(Character.MATH_SYMBOL, Character.getType(PLUS_MINUS.codePointAt(0)));
        assertEquals(Character.MATH_SYMBOL, Character.getType(N_ARY_SUMMATION.codePointAt(0)));
        assertEquals(Character.MATH_SYMBOL, Character.getType(SUPP_SM.codePointAt(0)));
        assertEquals(Character.MODIFIER_SYMBOL, Character.getType(DIAERESIS.codePointAt(0)));
        assertEquals(Character.MODIFIER_SYMBOL, Character.getType(UP_ARROWHEAD.codePointAt(0)));
        assertEquals(Character.MODIFIER_SYMBOL, Character.getType(SUPP_SK.codePointAt(0)));
        assertEquals(Character.OTHER_SYMBOL, Character.getType(DEGREE.codePointAt(0)));
        assertEquals(Character.OTHER_SYMBOL, Character.getType(SNOWMAN.codePointAt(0)));
        assertEquals(Character.OTHER_SYMBOL, Character.getType(SUPP_SO.codePointAt(0)));
        assertEquals(Character.UPPERCASE_LETTER, Character.getType(SUPP_LETTER.codePointAt(0)));
        assertEquals(Character.OTHER_PUNCTUATION, Character.getType(SUPP_PUNCT.codePointAt(0)));
    }

    // --- exhaustive sweep over every non-ASCII S and P code point ---

    /**
     * For each non-ASCII code point of a {@code P} or {@code S} category the closing {@code _} of {@code _foo_X}
     * closes if X is punctuation and the {@code *} of {@code a*Xfoo*} cannot open if X is punctuation.
     */
    @Test
    public void everyNonAsciiSymbolAndPunctuationCodePoint() {
        DataHolder newOptions = DEFAULT;
        DataHolder oldOptions = ParserEmulationProfile.COMMONMARK_0_30.getProfileOptions().toImmutable();
        Parser newParser = Parser.builder(newOptions).build();
        HtmlRenderer newRenderer = HtmlRenderer.builder(newOptions).build();
        Parser oldParser = Parser.builder(oldOptions).build();
        HtmlRenderer oldRenderer = HtmlRenderer.builder(oldOptions).build();

        int symbols = 0;
        int punctuation = 0;
        for (int c = 0x80; c <= Character.MAX_CODE_POINT; c++) {
            boolean symbol = isSymbol(c);
            boolean punct = isPunctuationCategory(c);
            if (!symbol && !punct) continue;
            if (symbol) symbols++;
            else punctuation++;

            String s = cp(c);
            String name = "U+" + Integer.toHexString(c).toUpperCase();
            String closes = p("<em>foo</em>" + s);
            String literal = p("_foo_" + s);
            assertEquals(name + " new, closing _", closes, newRenderer.render(newParser.parse("_foo_" + s)));
            assertEquals(name + " old, closing _", punct ? closes : literal, oldRenderer.render(oldParser.parse("_foo_" + s)));

            String cannotOpen = p("a*" + s + "foo*");
            String opens = p("a<em>" + s + "foo</em>");
            assertEquals(name + " new, opening *", cannotOpen, newRenderer.render(newParser.parse("a*" + s + "foo*")));
            assertEquals(name + " old, opening *", punct ? cannotOpen : opens, oldRenderer.render(oldParser.parse("a*" + s + "foo*")));
        }

        // JDK 21 knows Unicode 15: guard against a sweep which silently covers nothing
        assertTrue("symbols " + symbols, symbols > 7000);
        assertTrue("punctuation " + punctuation, punctuation > 500);
    }

    // --- Markdown to Markdown round trip ---

    private static void assertRoundTrip(ParserEmulationProfile profile, String markdown) {
        DataHolder options = options(profile);
        String formatted = format(options, markdown);
        assertEquals(profile + " format " + markdown, render(options, markdown), render(options, formatted));
    }

    @Test
    public void roundTripFormatterKeepsTheMeaning() {
        for (String s : SYMBOLS) {
            for (String markdown : new String[]{
                    "a*" + s + "foo*", "*foo" + s + "*a", "*" + s + "*alpha.", "_foo_" + s, s + "_foo_",
                    "x *" + s + "foo* y", "**" + s + "foo**" + s, s + "__foo__" + s,
            }) {
                for (ParserEmulationProfile profile : NEW_BEHAVIOUR) {
                    assertRoundTrip(profile, markdown);
                }
                for (ParserEmulationProfile profile : OLD_BEHAVIOUR) {
                    assertRoundTrip(profile, markdown);
                }
            }
        }
    }

    // --- strong emphasis, same flanking rules ---

    @Test
    public void strongEmphasisFollowsTheSameRules() {
        for (String s : new String[]{EURO, N_ARY_SUMMATION, DEGREE, SUPP_SO, SUPP_SM}) {
            assertNew(p("a**" + s + "foo**"), "a**" + s + "foo**");
            assertOld(p("a<strong>" + s + "foo</strong>"), "a**" + s + "foo**");
            assertNew(p("<strong>foo</strong>" + s), "__foo__" + s);
            assertOld(p("__foo__" + s), "__foo__" + s);
        }
    }

    // --- INLINE_DELIMITER_DIRECTIONAL_PUNCTUATIONS smoke tests ---

    private static DataHolder directionalOptions(ParserEmulationProfile profile) {
        return new MutableDataSet(options(profile)).set(Parser.INLINE_DELIMITER_DIRECTIONAL_PUNCTUATIONS, true).toImmutable();
    }

    @Test
    public void directionalPunctuationsDoesNotBreakSymbolsAndLettersAreStillLetters() {
        ParserEmulationProfile[] all = {null, ParserEmulationProfile.COMMONMARK_0_31_2, ParserEmulationProfile.COMMONMARK_0_30,
                ParserEmulationProfile.COMMONMARK_0_29, ParserEmulationProfile.COMMONMARK_0_26};
        for (ParserEmulationProfile profile : all) {
            DataHolder options = directionalOptions(profile);
            for (String l : new String[]{ASCII_LETTER, BMP_LETTER, SUPP_LETTER}) {
                assertEquals(String.valueOf(profile), p("a<em>" + l + "foo</em>"), render(options, "a*" + l + "foo*"));
                assertEquals(String.valueOf(profile), p("_foo_" + l), render(options, "_foo_" + l));
            }
            // smoke: symbols, BMP and supplementary, parse and render without failing and keep their text
            for (String s : SYMBOLS) {
                String html = render(options, "a*" + s + "foo* _bar_" + s + " " + s + "_baz_");
                assertNotNull(html);
                assertTrue(html, html.contains(s));
                assertFalse(html, html.contains("\uFFFD"));
            }
        }
    }

    // smoke: every symbol code point is accepted by the parser with the option on, no exception
    @Test
    public void directionalPunctuationsSurvivesTheExhaustiveSweep() {
        DataHolder options = directionalOptions(null);
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
        for (int c = 0x80; c <= Character.MAX_CODE_POINT; c++) {
            if (!isSymbol(c)) continue;
            String s = cp(c);
            assertNotNull(renderer.render(parser.parse("a*" + s + "foo* _foo_" + s + " " + s + "_foo_")));
        }
    }
}
