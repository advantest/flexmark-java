package com.vladsch.flexmark;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class FlexmarkInfoTest {
    final private static String TEST_RESOURCES = "/com/vladsch/flexmark/FlexmarkInfoTest/";

    @Test
    public void test_getVersion_answersProjectVersion() {
        String expected = System.getProperty("expectedVersion");
        assertNotNull("system property expectedVersion is not set", expected);
        assertEquals(expected, FlexmarkInfo.getVersion());
    }

    @Test
    public void test_getVersion_isFiltered() {
        String version = FlexmarkInfo.getVersion();
        assertNotNull(version);
        assertFalse(version, version.contains("${"));
    }

    @Test
    public void test_readVersion_missingResource() {
        assertNull(FlexmarkInfo.readVersion(TEST_RESOURCES + "does-not-exist.properties"));
    }

    @Test
    public void test_readVersion_unfilteredValue() {
        assertNull(FlexmarkInfo.readVersion(TEST_RESOURCES + "unfiltered.properties"));
    }

    @Test
    public void test_readVersion_missingKey() {
        assertNull(FlexmarkInfo.readVersion(TEST_RESOURCES + "missing-key.properties"));
    }
}
