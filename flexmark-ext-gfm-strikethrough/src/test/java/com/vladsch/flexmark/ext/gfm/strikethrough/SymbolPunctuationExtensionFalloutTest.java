package com.vladsch.flexmark.ext.gfm.strikethrough;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Pins the extension fallout of the CommonMark 0.31.2 change which makes Unicode symbols (categories Sc, Sk, Sm, So)
 * count as punctuation for emphasis flanking. The core scans delimiters centrally, so every delimiter processor,
 * here strikethrough and subscript, inherits the change. No spec example covers it because the spec has no
 * extensions. The new behaviour is intended and deliberately differs from GitHub (cmark-gfm, CommonMark 0.29), which
 * still renders {@code a<del>©x</del>b}. Users who need the old output set
 * {@link Parser#UNICODE_PUNCTUATION_INCLUDES_SYMBOLS} to {@code false}, which this test pins as being identical to
 * the 0.30 profile.
 */
public class SymbolPunctuationExtensionFalloutTest {
    // input, 0.30 profile / symbols off, 0.31.2 default
    private static final String[][] ROWS = {
            {"a~~,x~~b", "<p>a~~,x~~b</p>\n", "<p>a~~,x~~b</p>\n"},
            {"a~~\u00abx~~b", "<p>a~~\u00abx~~b</p>\n", "<p>a~~\u00abx~~b</p>\n"},
            {"a~~\u00a9x~~b", "<p>a<del>\u00a9x</del>b</p>\n", "<p>a~~\u00a9x~~b</p>\n"},
            {"a~~\u20acx~~b", "<p>a<del>\u20acx</del>b</p>\n", "<p>a~~\u20acx~~b</p>\n"},
            {"a*\u00a9x*b", "<p>a<em>\u00a9x</em>b</p>\n", "<p>a*\u00a9x*b</p>\n"},
            {"H~\u00b0~O", "<p>H<sub>\u00b0</sub>O</p>\n", "<p>H~\u00b0~O</p>\n"},
            {"H~2~O", "<p>H<sub>2</sub>O</p>\n", "<p>H<sub>2</sub>O</p>\n"},
            {"a~~x~~b", "<p>a<del>x</del>b</p>\n", "<p>a<del>x</del>b</p>\n"},
            {"~~\u00a9x~~", "<p><del>\u00a9x</del></p>\n", "<p><del>\u00a9x</del></p>\n"},
    };

    private static String render(ParserEmulationProfile profile, Boolean symbols, String input) {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Collections.singleton(StrikethroughSubscriptExtension.create()));
        if (profile != null) profile.setIn(options);
        if (symbols != null) options.set(Parser.UNICODE_PUNCTUATION_INCLUDES_SYMBOLS, symbols);
        return HtmlRenderer.builder(options).build().render(Parser.builder(options).build().parse(input));
    }

    private static void check(String config, String expected, String actual, String input) {
        assertEquals("[" + config + "] input: " + input, expected, actual);
    }

    @Test
    public void profile030() {
        for (String[] row : ROWS) {
            check("COMMONMARK_0_30", row[1], render(ParserEmulationProfile.COMMONMARK_0_30, null, row[0]), row[0]);
        }
    }

    @Test
    public void plainDefaultsAreCommonMark0312() {
        for (String[] row : ROWS) {
            check("plain defaults", row[2], render(null, null, row[0]), row[0]);
        }
    }

    @Test
    public void explicitProfile0312AgreesWithPlainDefaults() {
        for (String[] row : ROWS) {
            check("COMMONMARK_0_31_2", row[2], render(ParserEmulationProfile.COMMONMARK_0_31_2, null, row[0]), row[0]);
            check("COMMONMARK_0_31_2 vs plain defaults", render(null, null, row[0]),
                    render(ParserEmulationProfile.COMMONMARK_0_31_2, null, row[0]), row[0]);
        }
    }

    @Test
    public void profile0312WithSymbolsOff() {
        for (String[] row : ROWS) {
            check("COMMONMARK_0_31_2 + UNICODE_PUNCTUATION_INCLUDES_SYMBOLS=false", row[1],
                    render(ParserEmulationProfile.COMMONMARK_0_31_2, false, row[0]), row[0]);
        }
    }

    @Test
    public void optOutIsEquivalentToProfile030() {
        for (String[] row : ROWS) {
            check("0.31.2 + symbols=false vs COMMONMARK_0_30",
                    render(ParserEmulationProfile.COMMONMARK_0_30, null, row[0]),
                    render(ParserEmulationProfile.COMMONMARK_0_31_2, false, row[0]), row[0]);
        }
    }

    @Test
    public void olderProfilesTreatSymbolsAsLetterLike() {
        for (ParserEmulationProfile profile : new ParserEmulationProfile[] {
                ParserEmulationProfile.COMMONMARK_0_28, ParserEmulationProfile.COMMONMARK_0_29}) {
            check(profile.name(), "<p>a<del>\u00a9x</del>b</p>\n", render(profile, null, "a~~\u00a9x~~b"), "a~~\u00a9x~~b");
            check(profile.name(), "<p>H<sub>\u00b0</sub>O</p>\n", render(profile, null, "H~\u00b0~O"), "H~\u00b0~O");
        }
    }
}
