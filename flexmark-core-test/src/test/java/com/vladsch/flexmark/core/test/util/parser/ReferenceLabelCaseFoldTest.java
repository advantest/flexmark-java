package com.vladsch.flexmark.core.test.util.parser;

import com.vladsch.flexmark.ast.LinkRef;
import com.vladsch.flexmark.ast.Reference;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.Escaping;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * CommonMark 0.30 (#582): link labels match if their normalized forms, after the Unicode case fold, are equal.
 * The case fold is approximated by lower casing, upper casing and lower casing again, see
 * {@link Escaping#normalizeReference(CharSequence, boolean, boolean)}. Only the lookup key is normalized, the
 * source text of labels is kept.
 */
public class ReferenceLabelCaseFoldTest {
    private static final ParserEmulationProfile[] PRE_030 = {
            ParserEmulationProfile.COMMONMARK_0_26,
            ParserEmulationProfile.COMMONMARK_0_27,
            ParserEmulationProfile.COMMONMARK_0_28,
            ParserEmulationProfile.COMMONMARK_0_29,
    };

    private static Document parse(DataHolder options, String markdown) {
        return options == null ? Parser.builder().build().parse(markdown) : Parser.builder(options).build().parse(markdown);
    }

    private static String render(DataHolder options, String markdown) {
        HtmlRenderer renderer = options == null ? HtmlRenderer.builder().build() : HtmlRenderer.builder(options).build();
        return renderer.render(parse(options, markdown));
    }

    // no options at all, the default (0.31.2) keeps the 0.30 behaviour
    private static void assert030(String expected, String markdown) {
        assertEquals(expected, render(null, markdown));
    }

    private static void assertPre030(String expected, String markdown) {
        for (ParserEmulationProfile profile : PRE_030) {
            assertEquals("profile " + profile, expected, render(profile.getProfileOptions().toImmutable(), markdown));
        }
    }

    @Test
    public void capitalSharpSMatchesSSIn030() {
        assert030("<p><a href=\"/url\">\u1E9E</a></p>\n", "[\u1E9E]\n\n[SS]: /url\n");
    }

    @Test
    public void capitalSharpSDoesNotMatchSSBefore030() {
        assertPre030("<p>[\u1E9E]</p>\n", "[\u1E9E]\n\n[SS]: /url\n");
    }

    @Test
    public void sharpSMatchesSSIn030() {
        assert030("<p><a href=\"/url\">\u00DF</a></p>\n", "[\u00DF]\n\n[SS]: /url\n");
        assert030("<p><a href=\"/url\">SS</a></p>\n", "[SS]\n\n[\u00DF]: /url\n");
    }

    @Test
    public void capitalAndSmallSharpSMatch() {
        assert030("<p><a href=\"/url\">\u1E9E</a></p>\n", "[\u1E9E]\n\n[\u00DF]: /url\n");
    }

    @Test
    public void ligatureMatchesItsLettersIn030() {
        assert030("<p><a href=\"/url\">\uFB01</a></p>\n", "[\uFB01]\n\n[FI]: /url\n");
    }

    @Test
    public void foldingAppliesToFullAndCollapsedReferences() {
        assert030("<p><a href=\"/url\">text</a> <a href=\"/url\">\u1E9E</a> <img src=\"/url\" alt=\"\u1E9E\" /></p>\n",
                "[text][\u1E9E] [\u1E9E][] ![\u1E9E]\n\n[SS]: /url\n");
    }

    @Test
    public void asciiCaseInsensitivityStillWorksEverywhere() {
        String expected = "<p><a href=\"/url\">FOO</a></p>\n";
        assert030(expected, "[FOO]\n\n[foo]: /url\n");
        assertPre030(expected, "[FOO]\n\n[foo]: /url\n");
    }

    @Test
    public void whitespaceIsCollapsedTogetherWithCaseFolding() {
        assert030("<p><a href=\"/url\">\u1E9E a b</a></p>\n", "[\u1E9E a b]\n\n[ss  \tA \n B ]: /url\n");
    }

    @Test
    public void labelsWhichDifferBeyondCaseDoNotMatch() {
        assert030("<p>[S]</p>\n", "[S]\n\n[SS]: /url\n");
    }

    @Test
    public void sourceTextOfLabelsIsKept() {
        Document document = parse(null, "[\u1E9E  x]\n\n[SS x]: /url\n");
        Node paragraph = document.getFirstChild();
        LinkRef linkRef = (LinkRef) paragraph.getFirstChild();
        assertEquals("[\u1E9E  x]", linkRef.getChars().toString());
        assertEquals("\u1E9E  x", linkRef.getReference().toString());
        Reference reference = (Reference) document.getLastChild();
        assertEquals("[SS x]: /url", reference.getChars().toString().trim());
        assertEquals("SS x", reference.getReference().toString());
        // only the lookup key is normalized
        assertTrue(Parser.REFERENCES.get(document).containsKey("ss x"));
    }

    @Test
    public void caseFoldFunctionIsAnApproximationOfUnicodeCaseFold() {
        assertEquals("ss", Escaping.normalizeReference("\u1E9E", true, true));
        assertEquals("ss", Escaping.normalizeReference("\u00DF", true, true));
        assertEquals("ss", Escaping.normalizeReference("SS", true, true));
        assertEquals("\u03C3\u03B1\u03C3", Escaping.normalizeReference("\u03A3\u0391\u03A3", true, true));
        assertEquals("fi", Escaping.normalizeReference("\uFB01", true, true));
        // the legacy normalization only lower cases
        assertEquals("\u00DF", Escaping.normalizeReference("\u1E9E", true, false));
        assertEquals("a b", Escaping.normalizeReference(" A \n\t B ", true, true));
        assertEquals("A B", Escaping.normalizeReference(" A \n\t B ", false, true));
    }
}