package com.vladsch.flexmark.core.test.util.parser;

import com.vladsch.flexmark.parser.MutableListOptions;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

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

    /**
     * The complete set of list options written by setIn must equal the profile's own list options, not just
     * the endOnDoubleBlank flag.
     */
    @Test
    public void everyProfileResolvesAllOfItsListOptions() {
        for (ParserEmulationProfile profile : ParserEmulationProfile.values()) {
            assertEquals("profile " + profile + " must resolve all of its list options into the data holder",
                    profile.getOptions(), new MutableListOptions(resolve(profile)));
        }
    }

    /**
     * The defaults of the list options keep the behaviour before CommonMark 0.29, which is enabled only by the
     * 0.29 profile through LISTS_NO_ITEM_AT_CODE_INDENT. Except for that option, default list options equal the
     * ones of the CommonMark latest profile.
     */
    @Test
    public void defaultListOptionsEqualTheCommonMarkLatestProfileOptionsExceptTheCommonMark029Ones() {
        MutableListOptions latest = ParserEmulationProfile.COMMONMARK_LATEST.getOptions();
        assertTrue(latest.isNoItemAtCodeIndent());
        latest.setNoItemAtCodeIndent(false);
        assertEquals(latest, new MutableListOptions((com.vladsch.flexmark.util.data.DataHolder) null));
    }

    @Test
    public void versionedCommonMarkProfilesBelongToTheCommonMarkFamily() {        for (ParserEmulationProfile profile : new ParserEmulationProfile[]{
                ParserEmulationProfile.COMMONMARK_0_26,
                ParserEmulationProfile.COMMONMARK_0_27,
                ParserEmulationProfile.COMMONMARK_0_28,
                ParserEmulationProfile.COMMONMARK_0_29,
        }) {
            assertSame(ParserEmulationProfile.COMMONMARK, profile.family);
        }
    }

    /**
     * COMMONMARK_LATEST must alias a concrete implemented version, never the COMMONMARK family sentinel.
     */
    @Test
    public void commonMarkLatestAliasesAConcreteVersionProfile() {
        assertNotSame(ParserEmulationProfile.COMMONMARK, ParserEmulationProfile.COMMONMARK_LATEST);
        assertSame(ParserEmulationProfile.COMMONMARK, ParserEmulationProfile.COMMONMARK_LATEST.family);
    }

    @Test
    public void commonMarkLatestIsTheDefaultEmulationProfile() {
        assertSame(ParserEmulationProfile.COMMONMARK_LATEST, Parser.PARSER_EMULATION_PROFILE.get(null));
    }

    /**
     * Advancing COMMONMARK_LATEST to a newer specification version is only allowed once that version's full
     * specification test passes, and the default spec.txt resource must track it.
     */
    @Test
    public void commonMarkLatestMatchesTheDefaultSpecificationResource() throws Exception {
        assertSame("COMMONMARK_LATEST must be advanced together with spec.txt and VERSION.md",
                ParserEmulationProfile.COMMONMARK_0_29, ParserEmulationProfile.COMMONMARK_LATEST);
        assertArrayEquals("spec.txt must be a copy of the spec file of the version COMMONMARK_LATEST points at",
                readSpecResource("/spec.0.29.txt"), readSpecResource("/spec.txt"));
    }

    private static byte[] readSpecResource(String name) throws Exception {
        try (InputStream stream = ParserEmulationProfileTest.class.getResourceAsStream(name)) {
            assertNotNull("missing specification resource " + name, stream);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = stream.read(buffer)) != -1) out.write(buffer, 0, read);
            return out.toByteArray();
        }
    }
}
