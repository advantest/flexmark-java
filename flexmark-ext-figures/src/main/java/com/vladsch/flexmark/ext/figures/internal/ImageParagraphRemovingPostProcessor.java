/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.figures.internal;

import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.parser.block.NodePostProcessor;
import com.vladsch.flexmark.parser.block.NodePostProcessorFactory;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeTracker;
import com.vladsch.flexmark.util.data.DataHolder;
import org.jetbrains.annotations.NotNull;

public class ImageParagraphRemovingPostProcessor extends NodePostProcessor {
	
    @Override
    public void process(@NotNull NodeTracker state, @NotNull Node node) {
        if (node instanceof Image) {
            Image image = (Image) node;
            
            // Is image the only child of a paragraph?
            if (image.getParent() instanceof Paragraph
                    && image.getParent().getFirstChild() == image
                    && image.getParent().getLastChild() == image) {
            	Paragraph parentParagraph = (Paragraph) image.getParent();
            	
            	parentParagraph.insertAfter(image);
            	parentParagraph.unlink();
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
            return new ImageParagraphRemovingPostProcessor();
        }
    }
}
