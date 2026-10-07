package com.vladsch.flexmark.ext.tables;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
public class TablesParsingOptionsTest {
    private static String render(String markdown, DataHolder options) {
        MutableDataSet set = new MutableDataSet(options);
        set.set(Parser.EXTENSIONS, Collections.singletonList(TablesExtension.create()));
        Parser parser = Parser.builder(set).build();
        return HtmlRenderer.builder(set).build().render(parser.parse(markdown));
    }

    private static String body(String... rows) {
        return "<table>\n<thead>\n" + rows[0] + "\n</thead>\n<tbody>\n" + rows[1] + "\n</tbody>\n</table>\n";
    }

    private static MutableDataSet with(com.vladsch.flexmark.util.data.DataKey<Boolean> key, boolean value) {
        return new MutableDataSet().set(key, value);
    }

    @Test
    public void test_aShortBodyRowKeepsItsCellsByDefault() {
        String markdown = "| a | b | c |\n|---|---|---|\n| 1 | 2 |\n";

        assertEquals(body("<tr><th>a</th><th>b</th><th>c</th></tr>", "<tr><td>1</td><td>2</td></tr>"),
                render(markdown, new MutableDataSet()));
    }

    @Test
    public void test_aShortBodyRowIsPaddedWithEmptyCellsWhenMissingColumnsAreAppended() {
        String markdown = "| a | b | c |\n|---|---|---|\n| 1 | 2 |\n";

        assertEquals(body("<tr><th>a</th><th>b</th><th>c</th></tr>", "<tr><td>1</td><td>2</td><td></td></tr>"),
                render(markdown, with(TablesExtension.APPEND_MISSING_COLUMNS, true)));
    }

    @Test
    public void test_aLongBodyRowKeepsItsExtraCellByDefault() {
        String markdown = "| a | b |\n|---|---|\n| 1 | 2 | 3 |\n";

        assertEquals(body("<tr><th>a</th><th>b</th></tr>", "<tr><td>1</td><td>2</td><td>3</td></tr>"),
                render(markdown, new MutableDataSet()));
    }

    @Test
    public void test_aLongBodyRowLosesItsExtraCellWhenExtraColumnsAreDiscarded() {
        String markdown = "| a | b |\n|---|---|\n| 1 | 2 | 3 |\n";

        assertEquals(body("<tr><th>a</th><th>b</th></tr>", "<tr><td>1</td><td>2</td></tr>"),
                render(markdown, with(TablesExtension.DISCARD_EXTRA_COLUMNS, true)));
    }

    @Test
    public void test_aHeaderWithFewerColumnsThanTheSeparatorIsATableByDefault() {
        String markdown = "| a |\n|---|---|\n| 1 | 2 |\n";

        assertTrue(render(markdown, new MutableDataSet()).startsWith("<table>"));
    }

    @Test
    public void test_aHeaderWithFewerColumnsThanTheSeparatorIsNotATableWhenColumnsMustMatch() {
        String markdown = "| a |\n|---|---|\n| 1 | 2 |\n";

        assertEquals("<p>| a |\n|---|---|\n| 1 | 2 |</p>\n",
                render(markdown, with(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)));
    }

    @Test
    public void test_twoHeaderRowsFormATableByDefault() {
        String markdown = "| h1 |\n| h2 |\n|---|\n| 1 |\n";

        String html = render(markdown, new MutableDataSet());

        assertTrue(html, html.contains("<tr><th>h1</th></tr>\n<tr><th>h2</th></tr>"));
    }

    @Test
    public void test_moreHeaderRowsThanAllowedMakeTheTextAParagraph() {
        String markdown = "| h1 |\n| h2 |\n|---|\n| 1 |\n";

        assertEquals("<p>| h1 |\n| h2 |\n|---|\n| 1 |</p>\n",
                render(markdown, new MutableDataSet().set(TablesExtension.MAX_HEADER_ROWS, 1)));
    }

    @Test
    public void test_anEscapedPipeStaysInsideItsCell() {
        String markdown = "| a \\| b | c |\n|---|---|\n| 1 | 2 |\n";

        assertEquals(body("<tr><th>a | b</th><th>c</th></tr>", "<tr><td>1</td><td>2</td></tr>"),
                render(markdown, new MutableDataSet()));
    }

    @Test
    public void test_aCaptionLineFollowingTheTableBecomesItsCaption() {
        String markdown = "| a | b |\n|---|---|\n| 1 | 2 |\n[caption]\n";

        assertEquals("<table>\n<thead>\n<tr><th>a</th><th>b</th></tr>\n</thead>\n<tbody>\n<tr><td>1</td><td>2</td></tr>\n"
                + "</tbody>\n<caption>caption</caption>\n</table>\n", render(markdown, new MutableDataSet()));
    }

    @Test
    public void test_adjacentPipesSpanTwoColumnsOnlyWhenColumnSpansAreEnabled() {
        String markdown = "| a | b |\n|---|---|\n| 1 || 2 |\n";

        assertEquals(body("<tr><th>a</th><th>b</th></tr>", "<tr><td colspan=\"2\">1</td><td>2</td></tr>"),
                render(markdown, new MutableDataSet()));
        assertEquals(body("<tr><th>a</th><th>b</th></tr>", "<tr><td>1</td><td></td><td>2</td></tr>"),
                render(markdown, with(TablesExtension.COLUMN_SPANS, false)));
    }
}