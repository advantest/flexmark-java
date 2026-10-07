package com.vladsch.flexmark.html2md.converter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class HtmlNodeConverterSubContext implements HtmlNodeConverterContext {
    private static final Logger LOG = LoggerFactory.getLogger(HtmlNodeConverterSubContext.class);
    final protected HtmlMarkdownWriter markdown;
    NodeRenderingHandlerWrapper<?> renderingHandlerWrapper;
    @Nullable Node myRenderingNode;

    public HtmlNodeConverterSubContext(@NotNull HtmlMarkdownWriter markdown) {
        this.markdown = markdown;
        this.myRenderingNode = null;
        this.markdown.setContext(this);
    }

    public @Nullable Node getRenderingNode() {
        return myRenderingNode;
    }

    public void setRenderingNode(@Nullable Node renderingNode) {
        this.myRenderingNode = renderingNode;
    }

    public @NotNull HtmlMarkdownWriter getMarkdown() {
        return markdown;
    }

    public void flushTo(@NotNull Appendable out, int maxTrailingBlankLines) {
        flushTo(out, getHtmlConverterOptions().maxBlankLines, maxTrailingBlankLines);
    }

    public void flushTo(@NotNull Appendable out, int maxBlankLines, int maxTrailingBlankLines) {
        markdown.line();
        try {
            markdown.appendTo(out, maxBlankLines, maxTrailingBlankLines);
        } catch (IOException e) {
            LOG.warn("Converted Markdown could not be written to its destination, so the"
                    + " destination holds less than was converted.", e);
        }
    }
}
