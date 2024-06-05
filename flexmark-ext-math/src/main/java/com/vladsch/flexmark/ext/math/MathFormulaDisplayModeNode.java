/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.math;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.vladsch.flexmark.ast.CodeBlock;
import com.vladsch.flexmark.util.ast.BlockContent;
import com.vladsch.flexmark.util.sequence.BasedSequence;

/**
 * Node representing display mode math formula like the following
 *
 * <pre>
 *     $$\sum_{i=1}^{n}=\frac{n(n+1)}{2}$$
 * </pre>
 *
 */
public class MathFormulaDisplayModeNode extends CodeBlock {
	
	private BasedSequence startMarker;
    private BasedSequence endMarker;
    
    public MathFormulaDisplayModeNode() {
    }

    public MathFormulaDisplayModeNode(BasedSequence chars) {
        super(chars);
    }

    public MathFormulaDisplayModeNode(BasedSequence chars, List<BasedSequence> lineSegments) {
        super(chars, lineSegments);
    }

    public MathFormulaDisplayModeNode(List<BasedSequence> lineSegments) {
        this(getSpanningChars(lineSegments), lineSegments);
    }

    private static @NotNull BasedSequence getSpanningChars(@NotNull List<BasedSequence> lineSegments) {
        return lineSegments.isEmpty() ? BasedSequence.NULL : lineSegments.get(0).baseSubSequence(
                lineSegments.get(0).getStartOffset(),
                lineSegments.get(lineSegments.size() - 1).getEndOffset());
    }

    public MathFormulaDisplayModeNode(BlockContent blockContent) {
        super(blockContent);
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
