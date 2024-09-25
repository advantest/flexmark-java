/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.plantuml.internal;

import org.jetbrains.annotations.NotNull;

import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ext.plantuml.PlantUmlFencedCodeBlockNode;
import com.vladsch.flexmark.parser.block.NodePostProcessor;
import com.vladsch.flexmark.parser.block.NodePostProcessorFactory;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeTracker;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;

public class FencedCodeBlockReplacingPostProcessor extends NodePostProcessor {
	
    @Override
    public void process(@NotNull NodeTracker state, @NotNull Node node) {
        if (node instanceof FencedCodeBlock) {
            FencedCodeBlock fencedCodeBlock = (FencedCodeBlock) node;
            BasedSequence info = fencedCodeBlock.getInfo();
            if (info.equalsIgnoreCase("plantuml")) {
                PlantUmlFencedCodeBlockNode plantUmlFencedCodeBlock = new PlantUmlFencedCodeBlockNode(fencedCodeBlock);
                
                fencedCodeBlock.insertAfter(plantUmlFencedCodeBlock);
                fencedCodeBlock.unlink();
                plantUmlFencedCodeBlock.takeChildren(fencedCodeBlock);
                state.nodeAddedWithChildren(plantUmlFencedCodeBlock);
                state.nodeRemoved(fencedCodeBlock);
            }
        }
    }

    public static class Factory extends NodePostProcessorFactory {

        public Factory(DataHolder options) {
            super(false);

            addNodes(FencedCodeBlock.class);
        }

        @NotNull
        @Override
        public NodePostProcessor apply(@NotNull Document document) {
            return new FencedCodeBlockReplacingPostProcessor();
        }
    }
}
