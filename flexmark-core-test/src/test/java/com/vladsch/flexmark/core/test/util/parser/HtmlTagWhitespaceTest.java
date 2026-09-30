package com.vladsch.flexmark.core.test.util.parser;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.parser.Parser;
import org.junit.Test;

import static com.vladsch.flexmark.core.test.util.parser.Profile030Support.assert030;
import static com.vladsch.flexmark.core.test.util.parser.Profile030Support.assertPre030;
import static org.junit.Assert.assertEquals;

/**
 * CommonMark 0.30 (#618): whitespace in HTML tags is spaces, tabs and up to one line ending. Vertical tab (U+000B)
 * and form feed (U+000C) are no longer whitespace after a tag name, between attributes, around = or before the end
 * of a tag, and they do not end the tag name of HTML block starts.
 */
public class HtmlTagWhitespaceTest {
    private static final String VT = "\u000B";
    private static final String FF = "\u000C";

    // the control characters are not whitespace in a tag, so the tag is text, which is escaped
    private static void assertTagOnlyBefore030(String markdown, String textHtml) {
        assert030("<p>x " + textHtml + " y</p>\n", "x " + markdown + " y\n");
        assertPre030("<p>x " + markdown + " y</p>\n", "x " + markdown + " y\n");
    }

    @Test
    public void vtAfterTagNameEndsNoOpenTag() {
        assertTagOnlyBefore030("<a" + VT + "href=\"x\">", "&lt;a" + VT + "href=&quot;x&quot;&gt;");
    }

    @Test
    public void ffAfterTagNameEndsNoOpenTag() {
        assertTagOnlyBefore030("<a" + FF + "href=\"x\">", "&lt;a" + FF + "href=&quot;x&quot;&gt;");
    }

    @Test
    public void vtBetweenAttributesEndsNoOpenTag() {
        assertTagOnlyBefore030("<a b=\"1\"" + VT + "c=\"2\">", "&lt;a b=&quot;1&quot;" + VT + "c=&quot;2&quot;&gt;");
    }

    @Test
    public void ffAroundEqualsSignEndsNoOpenTag() {
        assertTagOnlyBefore030("<a b" + FF + "=" + FF + "c>", "&lt;a b" + FF + "=" + FF + "c&gt;");
    }

    @Test
    public void vtBeforeTagEndEndsNoOpenTag() {
        assertTagOnlyBefore030("<a" + VT + ">", "&lt;a" + VT + "&gt;");
        assertTagOnlyBefore030("<a b" + VT + "/>", "&lt;a b" + VT + "/&gt;");
    }

    @Test
    public void ffBeforeClosingTagEndEndsNoClosingTag() {
        assertTagOnlyBefore030("</a" + FF + ">", "&lt;/a" + FF + "&gt;");
    }

    @Test
    public void spaceTabAndLineEndingAreStillWhitespaceInTags() {
        String[] tags = {"<a href=\"x\">", "<a\thref=\"x\">", "<a\nhref=\"x\">", "<a b = c\t/>", "</a \t>", "<a\t>"};
        for (String tag : tags) {
            String expected = "<p>x " + tag + " y</p>\n";
            assert030(expected, "x " + tag + " y\n");
            assertPre030(expected, "x " + tag + " y\n");
        }
    }

    @Test
    public void vtIsNotWhitespaceAfterTypeSixBlockName() {
        assert030("<p>&lt;div" + VT + "foo&gt;</p>\n", "<div" + VT + "foo>\n");
        assertPre030("<div" + VT + "foo>\n", "<div" + VT + "foo>\n");
    }

    @Test
    public void ffIsNotWhitespaceAfterTypeSixBlockName() {
        assert030("<p>&lt;div" + FF + "&gt;</p>\n", "<div" + FF + ">\n");
        assertPre030("<div" + FF + ">\n", "<div" + FF + ">\n");
    }

    @Test
    public void tabAndSpaceEndTypeSixBlockName() {
        assert030("<div\tfoo>\n<div foo>\n", "<div\tfoo>\n<div foo>\n");
    }

    @Test
    public void vtIsNotWhitespaceAfterTypeOneBlockName() {
        assert030("<p>&lt;pre" + VT + "x&gt;\n<em>a</em></p>\n<p><em>b</em></p>\n", "<pre" + VT + "x>\n*a*\n\n*b*\n");
        assertPre030("<pre" + VT + "x>\n*a*\n\n*b*\n", "<pre" + VT + "x>\n*a*\n\n*b*\n");
    }

    @Test
    public void tabEndsTypeOneBlockName() {
        assert030("<pre\tx>\n*a*\n\n*b*\n", "<pre\tx>\n*a*\n\n*b*\n");
    }

    @Test
    public void trailingVtAfterATypeSevenTagIsNotAllowed() {
        assert030("<p><a>" + VT + "\nfoo</p>\n", "<a>" + VT + "\nfoo\n");
        assertPre030("<a>" + VT + "\nfoo\n", "<a>" + VT + "\nfoo\n");
    }

    @Test
    public void trailingSpacesAndTabsAfterATypeSevenTagAreAllowed() {
        assert030("<a> \t\nfoo\n", "<a> \t\nfoo\n");
    }

    @Test
    public void formatterRoundTripKeepsTheSource() {
        String markdown = "x <a" + VT + "href=\"x\"> <a\thref=\"x\"> y\n";
        assertEquals(markdown, Formatter.builder().build().render(Parser.builder().build().parse(markdown)));
    }
}