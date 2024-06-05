/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.figures;

import com.vladsch.flexmark.ext.figures.internal.ImageNodeRenderer;
import com.vladsch.flexmark.ext.figures.internal.ImageParagraphRemovingPostProcessor;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import org.jetbrains.annotations.NotNull;


public class FiguresExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {

    private FiguresExtension() {
    }

    public static FiguresExtension create() {
        return new FiguresExtension();
    }

    @Override
    public void parserOptions(MutableDataHolder options) {

    }

    @Override
    public void rendererOptions(@NotNull MutableDataHolder options) {

    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.postProcessorFactory(new ImageParagraphRemovingPostProcessor.Factory(parserBuilder));
    }

    @Override
    public void extend(@NotNull HtmlRenderer.Builder htmlRendererBuilder, @NotNull String rendererType) {
        if (htmlRendererBuilder.isRendererType("HTML")) {
            htmlRendererBuilder
                    .nodeRendererFactory(new ImageNodeRenderer.Factory());
        }
    }

}
