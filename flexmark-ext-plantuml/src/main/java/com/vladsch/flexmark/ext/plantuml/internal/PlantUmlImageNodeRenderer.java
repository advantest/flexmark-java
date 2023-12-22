package com.vladsch.flexmark.ext.plantuml.internal;

import com.vladsch.flexmark.ext.plantuml.PlantUmlExtension;
import com.vladsch.flexmark.ext.plantuml.PlantUmlImage;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlantUmlImageNodeRenderer implements NodeRenderer {

    private PlantUmlBlockNodeRenderer plantUmlRenderer = new PlantUmlBlockNodeRenderer();
    @Override
    public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        HashSet<NodeRenderingHandler<?>> set = new HashSet<>();
        set.add(new NodeRenderingHandler<>(PlantUmlImage.class, this::render));
        return set;
    }

    private void render(PlantUmlImage node, NodeRendererContext context, HtmlWriter htmlWriter) {
        Map<String, String> referencedFilesContents = PlantUmlExtension.KEY_DOCUMENT_PATH_TO_FILE_CONTENTS_MAP.get(context.getDocument());
        String targetUrl = node.getUrl().toString();

        String pumlFileContents = null;
        if (referencedFilesContents != null) {
            pumlFileContents = referencedFilesContents.get(targetUrl);
        }

        if (pumlFileContents == null) {
            try {
                String currentMdFilePath = PlantUmlExtension.KEY_DOCUMENT_FILE_PATH.get(context.getDocument());
                Path targetPath = new File(currentMdFilePath).toPath().getParent().resolve(Path.of(targetUrl));
                pumlFileContents = Files.readString(targetPath, StandardCharsets.UTF_8);
            } catch (Exception e) {
                e.printStackTrace();
                // TODO handle this properly
                plantUmlRenderer.renderPlantUmlCode(String.format("Could not read PlantUML file %s", targetUrl), htmlWriter, context);
                return;
            }
        }

        plantUmlRenderer.renderPlantUmlCode(pumlFileContents, htmlWriter, context);
    }

    public static class Factory implements NodeRendererFactory {
        @NotNull
        @Override
        public NodeRenderer apply(@NotNull DataHolder options) {
            return new PlantUmlImageNodeRenderer();
        }
    }
}
