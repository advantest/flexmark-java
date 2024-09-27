/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.plantuml.internal;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.vladsch.flexmark.ext.plantuml.PlantUmlFencedCodeBlockNode;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.data.DataHolder;

public class PlantUmlFencedCodeBlockRenderer extends PlantUmlBlockNodeRenderer {

    @Override
    public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        HashSet<NodeRenderingHandler<?>> set = new HashSet<>();
        set.add(new NodeRenderingHandler<>(PlantUmlFencedCodeBlockNode.class, this::render));
        return set;
    }

    private void render(PlantUmlFencedCodeBlockNode node, NodeRendererContext context, HtmlWriter htmlWriter) {
        renderPlantUmlCode(node.getContentChars().toString(), htmlWriter, context);
    }

    public static class Factory implements NodeRendererFactory {
        @NotNull
        @Override
        public NodeRenderer apply(@NotNull DataHolder options) {
            return new PlantUmlFencedCodeBlockRenderer();
        }
    }

}
