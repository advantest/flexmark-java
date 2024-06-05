/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.math;

import com.vladsch.flexmark.ext.math.internal.MathFormulaNodeRenderer;
import com.vladsch.flexmark.ext.math.internal.MathFormulaBlockParser;
import com.vladsch.flexmark.ext.math.internal.MathFormulaInLineDelimiterProcessor;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import org.jetbrains.annotations.NotNull;


public class MathExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {

    private MathExtension() {
    }

    public static MathExtension create() {
        return new MathExtension();
    }

    @Override
    public void parserOptions(MutableDataHolder options) {

    }

    @Override
    public void rendererOptions(@NotNull MutableDataHolder options) {

    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customBlockParserFactory(new MathFormulaBlockParser.Factory());
        parserBuilder.customDelimiterProcessor(new MathFormulaInLineDelimiterProcessor());
    }

    @Override
    public void extend(@NotNull HtmlRenderer.Builder htmlRendererBuilder, @NotNull String rendererType) {
        if (htmlRendererBuilder.isRendererType("HTML")) {
            htmlRendererBuilder.nodeRendererFactory(new MathFormulaNodeRenderer.Factory());
        }
    }

}
