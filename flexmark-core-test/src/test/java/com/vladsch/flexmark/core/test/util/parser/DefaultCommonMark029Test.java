package com.vladsch.flexmark.core.test.util.parser;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.data.DataHolder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * A parser built without applying any profile parses as CommonMark 0.31.2, which keeps all 0.29 rules. The profiles for older versions opt out.
 * One representative input for every option that differs between 0.28 and 0.29.
 */
public class DefaultCommonMark029Test {
    private static final ParserEmulationProfile[] PRE_029 = {
            ParserEmulationProfile.COMMONMARK_0_26,
            ParserEmulationProfile.COMMONMARK_0_27,
            ParserEmulationProfile.COMMONMARK_0_28,
    };

    private static String render(DataHolder options, String markdown) {
        return HtmlRenderer.builder(options).build().render(Parser.builder(options).build().parse(markdown));
    }

    private static void check(String markdown, String expected029, String expectedPre029) {
        assertEquals("plain builder", expected029, render(null, markdown));
        assertEquals("COMMONMARK_0_29", expected029, render(ParserEmulationProfile.COMMONMARK_0_29.getProfileOptions().toImmutable(), markdown));
        for (ParserEmulationProfile profile : PRE_029) {
            assertEquals("profile " + profile, expectedPre029, render(profile.getProfileOptions().toImmutable(), markdown));
        }
    }

    @Test
    public void fencedCodeTildeInfoAllowsBackticks() {
        check("~~~ aa ``` ~~~\nfoo\n~~~\n", "<pre><code class=\"language-aa\">foo\n</code></pre>\n", "<p>~~~ aa ``` ~~~\nfoo</p>\n<pre><code></code></pre>\n");
    }

    @Test
    public void codeSpanNormalization() {
        check("`  ``  `\n", "<p><code> `` </code></p>\n", "<p><code>``</code></p>\n");
    }

    @Test
    public void emphasisMultipleOfThree() {
        check("foo******bar*********baz\n", "<p>foo<strong><strong><strong>bar</strong></strong></strong>***baz</p>\n", "<p>foo******bar*********baz</p>\n");
    }

    @Test
    public void linkDestinationPointyBracketsAllowSpaces() {
        check("[a](<b c>)\n", "<p><a href=\"b c\">a</a></p>\n", "<p>[a](<b c>)</p>\n");
    }

    @Test
    public void linkDestinationNotStartingWithPointyBracket() {
        check("[a](<b)c)\n", "<p>[a](&lt;b)c)</p>\n", "<p><a href=\"&lt;b\">a</a>c)</p>\n");
    }

    @Test
    public void referenceDefinitionTitleRequiresSpace() {
        check("[foo]: <bar>(baz)\n\n[foo]\n", "<p>[foo]: <bar>(baz)</p>\n<p>[foo]</p>\n", "<p><a href=\"bar\" title=\"baz\">foo</a></p>\n");
    }

    @Test
    public void setextHeadingAfterReferenceDefinitions() {
        check("[foo]: /url\nBar\n===\n[foo]\n", "<h1>Bar</h1>\n<p><a href=\"/url\">foo</a></p>\n", "<h1>[foo]: /url\nBar</h1>\n<p>[foo]</p>\n");
    }

    @Test
    public void linkTitleParenthesesNoUnescapedOpening() {
        check("[a](/u (b(c))\n", "<p>[a](/u (b(c))</p>\n", "<p><a href=\"/u\" title=\"b(c\">a</a></p>\n");
    }

    @Test
    public void noListItemAtCodeIndent() {
        check("- a\n - b\n  - c\n   - d\n    - e\n", "<ul>\n<li>a</li>\n<li>b</li>\n<li>c</li>\n<li>d\n- e</li>\n</ul>\n", "<ul>\n<li>a</li>\n<li>b</li>\n<li>c</li>\n<li>d</li>\n<li>e</li>\n</ul>\n");
    }
}
