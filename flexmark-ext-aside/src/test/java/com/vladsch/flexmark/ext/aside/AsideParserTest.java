package com.vladsch.flexmark.ext.aside;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.SpecialLeadInHandler;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

final public class AsideParserTest {
    String escape(String input, Parser parser) {
        BasedSequence baseSeq = BasedSequence.of(input);
        List<SpecialLeadInHandler> handlers = Parser.SPECIAL_LEAD_IN_ESCAPER_LIST.get(parser.getOptions());
        StringBuilder sb = new StringBuilder();

        for (SpecialLeadInHandler handler : handlers) {
            handler.accept(baseSeq, sb::append);
            if (sb.length() > 0) return sb.toString();
        }
        return input;
    }

    String unEscape(String input, Parser parser) {
        BasedSequence baseSeq = BasedSequence.of(input);
        List<SpecialLeadInHandler> handlers = Parser.SPECIAL_LEAD_IN_UN_ESCAPER_LIST.get(parser.getOptions());
        StringBuilder sb = new StringBuilder();

        for (SpecialLeadInHandler handler : handlers) {
            handler.accept(baseSeq, sb::append);
            if (sb.length() > 0) return sb.toString();
        }
        return input;
    }

    @Test
    public void test_escape() {
        Parser parser = Parser.builder().extensions(Collections.singleton(AsideExtension.create())).build();

        assertEquals("abc", escape("abc", parser));
        assertEquals("\\|", escape("|", parser));
        assertEquals("\\|abc", escape("|abc", parser));

        assertEquals("abc", unEscape("abc", parser));
        assertEquals("|", unEscape("\\|", parser));
        assertEquals("|abc", unEscape("\\|abc", parser));
    }
}