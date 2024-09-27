/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.plantuml;

import com.vladsch.flexmark.ext.plantuml.internal.FencedCodeBlockReplacingPostProcessor;
import com.vladsch.flexmark.ext.plantuml.internal.ImageReplacingPostProcessor;
import com.vladsch.flexmark.ext.plantuml.internal.PlantUmlBlockNodeRenderer;
import com.vladsch.flexmark.ext.plantuml.internal.PlantUmlCodeBlockParser;
import com.vladsch.flexmark.ext.plantuml.internal.PlantUmlFencedCodeBlockRenderer;
import com.vladsch.flexmark.ext.plantuml.internal.PlantUmlImageNodeRenderer;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.NullableDataKey;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PlantUmlExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {

    public static final NullableDataKey<String> KEY_DOCUMENT_FILE_PATH = new NullableDataKey<>("DOC_FILE_PATH");
    public static final NullableDataKey<Map<String,String>> KEY_DOCUMENT_PATH_TO_FILE_CONTENTS_MAP = new NullableDataKey<>("RELATIVE_PATH_TO_FILE_CONTENTS_MAP");
    public static final NullableDataKey<Boolean> KEY_RENDER_FENCED_PLANTUML_CODE_BLOCKS = new NullableDataKey<Boolean>("RENDER_FENCED_PLANUML_CODE_BLOCKS", false);

    private PlantUmlExtension() {
    }

    public static PlantUmlExtension create() {
        return new PlantUmlExtension();
    }

    @Override
    public void parserOptions(MutableDataHolder options) {

    }

    @Override
    public void rendererOptions(@NotNull MutableDataHolder options) {

    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customBlockParserFactory(new PlantUmlCodeBlockParser.Factory())
            .postProcessorFactory(new ImageReplacingPostProcessor.Factory(parserBuilder))
            .postProcessorFactory(new FencedCodeBlockReplacingPostProcessor.Factory(parserBuilder));
    }

    @Override
    public void extend(@NotNull HtmlRenderer.Builder htmlRendererBuilder, @NotNull String rendererType) {
        if (htmlRendererBuilder.isRendererType("HTML")) {
            htmlRendererBuilder
                    .nodeRendererFactory(new PlantUmlBlockNodeRenderer.Factory())
                    .nodeRendererFactory(new PlantUmlImageNodeRenderer.Factory())
                    .nodeRendererFactory(new PlantUmlFencedCodeBlockRenderer.Factory());
        }
    }

}
