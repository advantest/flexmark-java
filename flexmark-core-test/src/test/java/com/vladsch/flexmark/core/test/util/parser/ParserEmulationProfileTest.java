package com.vladsch.flexmark.core.test.util.parser;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

public class ParserEmulationProfileTest {
    private static MutableDataSet resolve(ParserEmulationProfile profile) {
        MutableDataSet options = new MutableDataSet();
        profile.setIn(options);
        return options;
    }

    @Test
    public void everyProfileRecordsItsOwnIdentity() {
        for (ParserEmulationProfile profile : ParserEmulationProfile.values()) {
            assertSame("profile " + profile + " must record itself in PARSER_EMULATION_PROFILE",
                    profile, Parser.PARSER_EMULATION_PROFILE.get(resolve(profile)));
        }
    }

    @Test
    public void everyProfileResolvesItsListOptions() {
        for (ParserEmulationProfile profile : ParserEmulationProfile.values()) {
            assertEquals("profile " + profile + " must resolve its list options into the data holder",
                    profile.getOptions().isEndOnDoubleBlank(),
                    Parser.LISTS_END_ON_DOUBLE_BLANK.get(resolve(profile)));
        }
    }

    /**
     * CommonMark 0.26 removed the "two blank lines end a list" rule, so the 0.26 profile must not enable it.
     * See the 0.25 to 0.26 specification diff at https://spec.commonmark.org/0.26/changes.html
     */
    @Test
    public void commonMark_0_26_doesNotEndListsOnDoubleBlank() {
        assertFalse(ParserEmulationProfile.COMMONMARK_0_26.getOptions().isEndOnDoubleBlank());
        assertFalse(Parser.LISTS_END_ON_DOUBLE_BLANK.get(resolve(ParserEmulationProfile.COMMONMARK_0_26)));
    }

    @Test
    public void versionedCommonMarkProfilesBelongToTheCommonMarkFamily() {
        for (ParserEmulationProfile profile : new ParserEmulationProfile[]{
                ParserEmulationProfile.COMMONMARK_0_26,
                ParserEmulationProfile.COMMONMARK_0_27,
                ParserEmulationProfile.COMMONMARK_0_28,
                ParserEmulationProfile.COMMONMARK_0_29,
        }) {
            assertSame(ParserEmulationProfile.COMMONMARK, profile.family);
        }
    }
}
