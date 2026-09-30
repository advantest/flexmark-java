package com.vladsch.flexmark.core.test.util.parser;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * CommonMark 0.29 (#487): decimal numeric character references have at most 7 digits and hexadecimal ones at most 6
 * digits. The digit count governs, not the value, so leading zeros count. Longer sequences stay literal text.
 */
public class NumericCharacterReferenceTest {
    private static String render(DataHolder options, String markdown) {
        return HtmlRenderer.builder(options).build().render(Parser.builder(options).build().parse(markdown));
    }

    private static void assertRenders(String expected, String markdown) {
        assertEquals("default options", expected, render(new MutableDataSet().toImmutable(), markdown));
        DataHolder options = ParserEmulationProfile.COMMONMARK_0_29.getProfileOptions().toImmutable();
        assertEquals("COMMONMARK_0_29", expected, render(options, markdown));
    }

    private static void assertPara(String expected, String markdown) {
        assertRenders("<p>" + expected + "</p>\n", markdown);
    }

    @Test
    public void decimalSevenDigitsIsReference() {
        assertPara("A", "&#0000065;\n");
        assertPara("\uD83D\uDE00", "&#0128512;\n");
    }

    @Test
    public void decimalEightDigitsIsLiteral() {
        assertPara("&amp;#87654321;", "&#87654321;\n");
        assertPara("&amp;#00000065;", "&#00000065;\n");
    }

    @Test
    public void decimalLongerIsLiteral() {
        assertPara("&amp;#987654321;", "&#987654321;\n");
    }

    @Test
    public void hexSixDigitsIsReference() {
        assertPara("A", "&#x000041;\n");
        assertPara("\uD83D\uDE00", "&#x01F600;\n");
        assertPara("\uD83D\uDE00", "&#X1F600;\n");
        assertPara("\uDBFF\uDFFF", "&#x10FFFF;\n");
    }

    @Test
    public void hexSevenDigitsIsLiteral() {
        assertPara("&amp;#x1234567;", "&#x1234567;\n");
        assertPara("&amp;#x0000041;", "&#x0000041;\n");
    }

    @Test
    public void hexEightDigitsIsLiteral() {
        assertPara("&amp;#x12345678;", "&#x12345678;\n");
    }

    @Test
    public void nullCharacterIsReplacement() {
        assertPara("\uFFFD", "&#0;\n");
        assertPara("\uFFFD", "&#x0;\n");
        assertPara("\uFFFD", "&#0000000;\n");
    }

    @Test
    public void outOfRangeButShortEnoughIsReplacement() {
        assertPara("\uFFFD", "&#1114112;\n");
        assertPara("\uFFFD", "&#9999999;\n");
        assertPara("\uFFFD", "&#x110000;\n");
        assertPara("\uFFFD", "&#xFFFFFF;\n");
    }

    @Test
    public void validReferencesStillWork() {
        assertPara("# &quot; &amp;", "&#35; &#x22; &amp;\n");
        assertPara("# &quot; &quot;", "&#35; &#X22; &#x22;\n");
    }

    @Test
    public void mixedWithLiteralInOneLine() {
        assertPara("A&amp;#87654321;B", "&#65;&#87654321;B\n");
    }

    @Test
    public void linkDestination() {
        assertRenders("<p><a href=\"/uA\">a</a></p>\n", "[a](/u&#0000065;)\n");
        assertRenders("<p><a href=\"/uA\">a</a></p>\n", "[a](/u&#x000041;)\n");
        assertRenders("<p><a href=\"/u&amp;#87654321;\">a</a></p>\n", "[a](/u&#87654321;)\n");
        assertRenders("<p><a href=\"/u&amp;#x1234567;\">a</a></p>\n", "[a](/u&#x1234567;)\n");
    }

    @Test
    public void codeSpanNeverDecodes() {
        assertPara("<code>&amp;#65;</code>", "`&#65;`\n");
        assertPara("<code>&amp;#87654321;</code>", "`&#87654321;`\n");
        assertPara("<code>&amp;#x1234567;</code>", "`&#x1234567;`\n");
    }
}
