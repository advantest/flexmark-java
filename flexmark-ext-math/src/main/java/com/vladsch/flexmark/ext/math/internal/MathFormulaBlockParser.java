/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.math.internal;

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ext.math.MathFormulaDisplayModeNode;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import com.vladsch.flexmark.parser.block.*;

public class MathFormulaBlockParser extends AbstractBlockParser {

	private final MathFormulaDisplayModeNode blockNode;
    private final BlockData blockData;
    
    public MathFormulaBlockParser(BlockData blockData) {
        if (blockData == null) {
            throw new IllegalArgumentException();
        }
        
        this.blockData = blockData;
        this.blockNode = new MathFormulaDisplayModeNode();
        this.blockNode.setStartMarker(blockData.startMarker);
    }

    @Override
    public MathFormulaDisplayModeNode getBlock() {
        return blockNode;
    }
    
    @Override
    public BlockContinue tryContinue(ParserState state) {
        if (this.blockData.finished) {
            this.blockNode.setEndMarker(blockData.endMarker);
            this.blockNode.setChars(blockData.contents);
            return BlockContinue.finished();
        }
        
        // read next line if we didn't see the end marker yet
        BasedSequence line = state.getLine();
        int indexOfStartMarker = line.indexOf("$$");
        if (indexOfStartMarker >= 0) {
            this.blockData.finished = true;
        }
        return BlockContinue.atColumn(state.getColumn());
    }
    
    @Override
    public void addLine(ParserState state, BasedSequence line) {
        if (this.blockData.contents == null) {
            this.blockData.contents = line;
        } else {
            this.blockData.contents = this.blockData.contents.append(line);
        }
    }
    
    @Override
    public void closeBlock(ParserState state) {
        if (this.blockNode.getParent() != null) {
            BasedSequence start = blockNode.getStartMarker();
            BasedSequence end = blockNode.getEndMarker();
            if (end == null && this.blockData.endMarker != null) {
                blockNode.setEndMarker(this.blockData.endMarker);
                end = blockNode.getEndMarker();
            }

            if (start != null && end != null
                    && start.length() > 1 && end.length() > 1) {
                blockNode.setChars(this.blockData.contents);
                
                Text textNode = new Text(blockNode.baseSubSequence(start.getEndOffset(), end.getStartOffset()));
                blockNode.appendChild(textNode);
            }
        }
    }
    
    public static class Factory implements CustomBlockParserFactory {
        @Nullable
        @Override
        public Set<Class<?>> getAfterDependents() {
            return null;
        }

        @Nullable
        @Override
        public Set<Class<?>> getBeforeDependents() {
            return null;
        }

        @Override
        public boolean affectsGlobalScope() {
            return false;
        }

        @NotNull
        @Override
        public BlockParserFactory apply(@NotNull DataHolder options) {
            return new BlockFactory(options);
        }
    }
    
    private static class BlockFactory extends AbstractBlockParserFactory {

        BlockFactory(DataHolder options) {
            super(options);
        }

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            BasedSequence line = state.getLine();
            int nextNoneSpaceCharIndex = state.getNextNonSpaceIndex();
            int indexOfStartMarker = line.indexOf("$$");
            if (indexOfStartMarker < 0 || indexOfStartMarker != nextNoneSpaceCharIndex) {
                return BlockStart.none();
            }

            int indexOfFirstFormulaSymbol = nextNoneSpaceCharIndex + 2;
            BlockData blockData = new BlockData(line.getStartOffset() + indexOfStartMarker,
                    line.subSequence(nextNoneSpaceCharIndex, indexOfFirstFormulaSymbol));
            
            // look ahead to see if we find a corresponding end marker for the code block
            
            if (line.length() > indexOfFirstFormulaSymbol) {
                BasedSequence lineRemainder = line.subSequence(indexOfFirstFormulaSymbol);
                int indexOfEndMarkerInLineRemainder = lineRemainder.lastIndexOf("$$");
                if (lineRemainder.indexOf("$$") != indexOfEndMarkerInLineRemainder) {
                    return BlockStart.none();
                }
                if (indexOfEndMarkerInLineRemainder > 0) {
                    int indexOfFirstEndMarkerChar = indexOfFirstFormulaSymbol + indexOfEndMarkerInLineRemainder;
                    blockData.endOffset = line.getStartOffset() + indexOfFirstEndMarkerChar + 2;
                    blockData.endMarker = line.subSequence(indexOfFirstEndMarkerChar, indexOfFirstEndMarkerChar + 2);
                    //blockData.contents = line.subSequence(indexOfFirstFormulaSymbol, indexOfFirstFormulaSymbol + indexOfEndMarkerInLineRemainder);
                    blockData.finished = true;
                    return BlockStart.of(new MathFormulaBlockParser(blockData))
                            .atColumn(state.getColumn());
                }
            }

            // go on with look ahead: check following lines
            BasedSequence parentSequence = state.getLine().getBaseSequence();
            BasedSequence[] remainingLines = parentSequence.subSequence(state.getLine().getEndOffset()).splitEOL();
            for (BasedSequence currentLine : remainingLines) {
                int indexOfEndMarker = currentLine.indexOf("$$");
                if (indexOfEndMarker > 0) {
                    blockData.endOffset = currentLine.getStartOffset() + indexOfEndMarker + 2;
                    blockData.endMarker = currentLine.subSequence(indexOfEndMarker, indexOfEndMarker + 2);
                    return BlockStart.of(new MathFormulaBlockParser(blockData))
                            .atColumn(state.getColumn());
                }
            }

            return BlockStart.none();
        }

    }
    
    private static class BlockData {
        int startOffset;
        int endOffset;
        BasedSequence startMarker;
        BasedSequence endMarker;
        BasedSequence contents;

        boolean finished = false;

        BlockData(int startOffset, BasedSequence startMarker) {
            this.startOffset = startOffset;
            this.startMarker = startMarker;
        }
    }
}
