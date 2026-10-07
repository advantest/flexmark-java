package com.vladsch.flexmark.formatter;

import com.vladsch.flexmark.util.ast.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public abstract class NodeFormatterSubContext implements NodeFormatterContext {
    private static final Logger LOG = LoggerFactory.getLogger(NodeFormatterSubContext.class);

    final protected MarkdownWriter markdown;
    Node renderingNode;
    List<NodeFormattingHandler<?>> rendererList = null;
    int rendererIndex = -1;

    public NodeFormatterSubContext(@NotNull MarkdownWriter markdown) {
        this.markdown = markdown;
        this.renderingNode = null;
    }

    public @Nullable Node getRenderingNode() {
        return renderingNode;
    }

    public void setRenderingNode(@Nullable Node renderingNode) {
        this.renderingNode = renderingNode;
    }

    @Override
    public @NotNull MarkdownWriter getMarkdown() {
        return markdown;
    }

    public void flushTo(@NotNull Appendable out, int maxTrailingBlankLines) {
        flushTo(out, getFormatterOptions().maxBlankLines, maxTrailingBlankLines);
    }

    public void flushTo(@NotNull Appendable out, int maxBlankLines, int maxTrailingBlankLines) {
        markdown.line();
        try {
            markdown.appendTo(out, maxBlankLines, maxTrailingBlankLines);
        } catch (IOException e) {
            LOG.warn("Formatted Markdown could not be written to its destination, so the"
                    + " destination holds less than was formatted.", e);
        }
    }
}
