/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.plantuml;

import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import java.util.List;

/**
 * Node representing code blocks like the following
 *
 * <pre>
 *     ```plantuml
 *     &#064;startuml
 *         title PlantUML version: %version()
 *
 *         Image --|> Node
 *         PlantUmlImage --|> Image
 *     &#064;enduml
 *     ```
 * </pre>
 *
 */
public class PlantUmlFencedCodeBlockNode extends FencedCodeBlock {

    private BasedSequence startMarker;
    private BasedSequence endMarker;

    public PlantUmlFencedCodeBlockNode() {
    }

    public PlantUmlFencedCodeBlockNode(BasedSequence chars) {
        super(chars);
    }

    public PlantUmlFencedCodeBlockNode(BasedSequence chars, BasedSequence openingMarker, BasedSequence info, List<BasedSequence> segments, BasedSequence closingMarker) {
        super(chars, openingMarker, info, segments, closingMarker);
    }
    
    public PlantUmlFencedCodeBlockNode(FencedCodeBlock fencedCodeBlock) {
    	this.setChars(fencedCodeBlock.getChars());
    	this.setOpeningMarker(fencedCodeBlock.getOpeningMarker());
    	this.setInfo(fencedCodeBlock.getInfo());
    	this.setAttributes(fencedCodeBlock.getAttributes());
    	this.setContentLines(fencedCodeBlock.getContentLines());
    	this.setClosingMarker(fencedCodeBlock.getClosingMarker());
    }

    public BasedSequence getStartMarker() {
        return startMarker;
    }

    public void setStartMarker(BasedSequence startMarker) {
        this.startMarker = startMarker;
    }

    public BasedSequence getEndMarker() {
        return endMarker;
    }

    public void setEndMarker(BasedSequence endMarker) {
        this.endMarker = endMarker;
    }

}