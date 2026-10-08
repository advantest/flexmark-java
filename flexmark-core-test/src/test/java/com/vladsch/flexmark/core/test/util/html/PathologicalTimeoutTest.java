package com.vladsch.flexmark.core.test.util.html;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Pins which machines get the wider pathological parsing budget, so that the decision stays visible and a later
 * change to it is deliberate rather than accidental.
 */
final public class PathologicalTimeoutTest {
    @Test
    public void macOnIntelGetsTheWiderBudget() {
        assertEquals(10, PathologicalRenderingTestCase.timeoutSeconds("Mac OS X", "x86_64"));
    }

    @Test
    public void macOnAppleSiliconKeepsTheNormalBudget() {
        assertEquals(3, PathologicalRenderingTestCase.timeoutSeconds("Mac OS X", "aarch64"));
    }

    @Test
    public void everyOtherMachineKeepsTheNormalBudget() {
        assertEquals(3, PathologicalRenderingTestCase.timeoutSeconds("Linux", "amd64"));
        assertEquals(3, PathologicalRenderingTestCase.timeoutSeconds("Linux", "x86_64"));
        assertEquals(3, PathologicalRenderingTestCase.timeoutSeconds("Windows 11", "amd64"));
        assertEquals(3, PathologicalRenderingTestCase.timeoutSeconds("Windows 11", "x86_64"));
    }

    @Test
    public void missingSystemPropertiesKeepTheNormalBudget() {
        assertEquals(3, PathologicalRenderingTestCase.timeoutSeconds("", ""));
    }
}
