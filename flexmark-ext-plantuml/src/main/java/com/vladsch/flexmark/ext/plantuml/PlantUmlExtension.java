package com.vladsch.flexmark.ext.plantuml;

import com.vladsch.flexmark.ext.plantuml.internal.ImageReplacingPostProcessor;
import com.vladsch.flexmark.ext.plantuml.internal.PlantUmlBlockNodeRenderer;
import com.vladsch.flexmark.ext.plantuml.internal.PlantUmlCodeBlockParser;
import com.vladsch.flexmark.ext.plantuml.internal.PlantUmlImageNodeRenderer;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.NullableDataKey;
import org.jetbrains.annotations.NotNull;

public class PlantUmlExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {

    final public static NullableDataKey<String> KEY_DOCUMENT_FILE_PATH = new NullableDataKey<>("DOC_FILE_PATH");

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
        parserBuilder.customBlockParserFactory(new PlantUmlCodeBlockParser.Factory());
        parserBuilder.postProcessorFactory(new ImageReplacingPostProcessor.Factory(parserBuilder));
    }

    @Override
    public void extend(@NotNull HtmlRenderer.Builder htmlRendererBuilder, @NotNull String rendererType) {
        if (htmlRendererBuilder.isRendererType("HTML")) {
            htmlRendererBuilder
                    .nodeRendererFactory(new PlantUmlBlockNodeRenderer.Factory())
                    .nodeRendererFactory(new PlantUmlImageNodeRenderer.Factory());
        }
    }

}
