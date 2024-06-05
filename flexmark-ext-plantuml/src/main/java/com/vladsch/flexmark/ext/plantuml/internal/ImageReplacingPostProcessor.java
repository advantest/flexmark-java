/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.plantuml.internal;

import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ext.plantuml.PlantUmlImage;
import com.vladsch.flexmark.parser.block.NodePostProcessor;
import com.vladsch.flexmark.parser.block.NodePostProcessorFactory;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeTracker;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

public class ImageReplacingPostProcessor extends NodePostProcessor {
    @Override
    public void process(@NotNull NodeTracker state, @NotNull Node node) {
        if (node instanceof Image) {
            Image image = (Image) node;
            BasedSequence url = image.getUrl();
            if (url.endsWith(".puml")) {
                PlantUmlImage plantUmlImage = new PlantUmlImage(image);

                if (image.getParent() instanceof Paragraph
                        && image.getParent().getFirstChild() == image
                        && image.getParent().getLastChild() == image) {
                    image.getParent().insertAfter(plantUmlImage);
                    image.getParent().unlink();
                    image.unlink();
                } else {
                    image.insertAfter(plantUmlImage);
                    image.unlink();
                }
                plantUmlImage.takeChildren(image);
                state.nodeAddedWithChildren(plantUmlImage);
                state.nodeRemoved(image);

                if (plantUmlImage.getPrevious() == null
                        && plantUmlImage.getNext() == null
                        && plantUmlImage.getParent() instanceof Paragraph) {
                    Paragraph parentParagraph = (Paragraph) plantUmlImage.getParent();
                    parentParagraph.insertAfter(plantUmlImage);
                }
            }
        }
    }

    public static class Factory extends NodePostProcessorFactory {

        public Factory(DataHolder options) {
            super(false);

            addNodes(Image.class);
        }

        @NotNull
        @Override
        public NodePostProcessor apply(@NotNull Document document) {
            return new ImageReplacingPostProcessor();
        }
    }
}
