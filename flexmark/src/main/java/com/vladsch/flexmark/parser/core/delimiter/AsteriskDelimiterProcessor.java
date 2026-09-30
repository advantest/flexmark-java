package com.vladsch.flexmark.parser.core.delimiter;

public class AsteriskDelimiterProcessor extends EmphasisDelimiterProcessor {

    public AsteriskDelimiterProcessor(boolean strongWrapsEmphasis) {
        super('*', strongWrapsEmphasis);
    }

    public AsteriskDelimiterProcessor(boolean strongWrapsEmphasis, boolean multipleOfThreeExemption) {
        super('*', strongWrapsEmphasis, multipleOfThreeExemption);
    }
}
