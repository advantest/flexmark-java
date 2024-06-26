/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.math.internal;

import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ext.math.MathFormulaInLineNode;
import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.core.delimiter.Delimiter;
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor;
import com.vladsch.flexmark.parser.delimiter.DelimiterRun;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;

public class MathFormulaInLineDelimiterProcessor implements DelimiterProcessor {

    @Override
    public char getOpeningCharacter() {
        return '$';
    }

    @Override
    public char getClosingCharacter() {
        return '$';
    }

    @Override
    public int getMinLength() {
        return 1;
    }
    
    @Override
    public boolean canBeOpener(String before, String after, boolean leftFlanking, boolean rightFlanking,
            boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace,
            boolean afterIsWhiteSpace) {
        return leftFlanking && beforeIsWhitespace;
    }

    @Override
    public boolean canBeCloser(String before, String after, boolean leftFlanking, boolean rightFlanking,
            boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace,
            boolean afterIsWhiteSpace) {
        return rightFlanking && afterIsWhiteSpace;
    }

    @Override
    public boolean skipNonOpenerCloser() {
        return true;
    }

    @Override
    public int getDelimiterUse(DelimiterRun opener, DelimiterRun closer) {
        if (opener.length() == 1 && closer.length() == 1) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public Node unmatchedDelimiterNode(InlineParser inlineParser, DelimiterRun delimiter) {
        return null;
    }

    @Override
    public void process(Delimiter opener, Delimiter closer, int delimitersUsed) {
        BasedSequence text = opener.getInput().subSequence(opener.getEndIndex(), closer.getStartIndex());
        MathFormulaInLineNode formula = new MathFormulaInLineNode(
                opener.getTailChars(delimitersUsed), text, closer.getLeadChars(delimitersUsed));
        opener.moveNodesBetweenDelimitersTo(formula, closer);
    }
    
}
