package com.vladsch.flexmark.core.test.util.parser;

import com.vladsch.flexmark.parser.ListOptions;
import com.vladsch.flexmark.parser.MutableListOptions;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class ListOptionsTest {
    @Test
    public void test_optionsWithTheSameSettingsAreEqualAndShareAHashCode() {
        ListOptions a = new MutableListOptions().setAutoLoose(false).setCodeIndent(6);
        ListOptions b = new MutableListOptions().setAutoLoose(false).setCodeIndent(6);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a, a);
        assertNotEquals(a, null);
        assertNotEquals(a, "options");
    }

    @Test
    public void test_changingAnySingleSettingMakesTheOptionsDifferent() {
        ListOptions base = new ListOptions();

        assertNotEquals(base, new MutableListOptions().setAutoLoose(!base.isAutoLoose()));
        assertNotEquals(base, new MutableListOptions().setAutoLooseOneLevelLists(!base.isAutoLooseOneLevelLists()));
        assertNotEquals(base, new MutableListOptions().setDelimiterMismatchToNewList(!base.isDelimiterMismatchToNewList()));
        assertNotEquals(base, new MutableListOptions().setEndOnDoubleBlank(!base.isEndOnDoubleBlank()));
        assertNotEquals(base, new MutableListOptions().setItemMarkerSpace(!base.isItemMarkerSpace()));
        assertNotEquals(base, new MutableListOptions().setItemTypeMismatchToNewList(!base.isItemTypeMismatchToNewList()));
        assertNotEquals(base, new MutableListOptions().setItemTypeMismatchToSubList(!base.isItemTypeMismatchToSubList()));
        assertNotEquals(base, new MutableListOptions().setLooseWhenPrevHasTrailingBlankLine(!base.isLooseWhenPrevHasTrailingBlankLine()));
        assertNotEquals(base, new MutableListOptions().setLooseWhenLastItemPrevHasTrailingBlankLine(!base.isLooseWhenLastItemPrevHasTrailingBlankLine()));
        assertNotEquals(base, new MutableListOptions().setLooseWhenHasNonListChildren(!base.isLooseWhenHasNonListChildren()));
        assertNotEquals(base, new MutableListOptions().setLooseWhenBlankLineFollowsItemParagraph(!base.isLooseWhenBlankLineFollowsItemParagraph()));
        assertNotEquals(base, new MutableListOptions().setLooseWhenHasLooseSubItem(!base.isLooseWhenHasLooseSubItem()));
        assertNotEquals(base, new MutableListOptions().setLooseWhenHasTrailingBlankLine(!base.isLooseWhenHasTrailingBlankLine()));
        assertNotEquals(base, new MutableListOptions().setLooseWhenContainsBlankLine(!base.isLooseWhenContainsBlankLine()));
        assertNotEquals(base, new MutableListOptions().setNumberedItemMarkerSuffixed(!base.isNumberedItemMarkerSuffixed()));
        assertNotEquals(base, new MutableListOptions().setOrderedItemDotOnly(!base.isOrderedItemDotOnly()));
        assertNotEquals(base, new MutableListOptions().setOrderedListManualStart(!base.isOrderedListManualStart()));
        assertNotEquals(base, new MutableListOptions().setCodeIndent(base.getCodeIndent() + 1));
        assertNotEquals(base, new MutableListOptions().setItemIndent(base.getItemIndent() + 1));
        assertNotEquals(base, new MutableListOptions().setNewItemCodeIndent(base.getNewItemCodeIndent() + 1));
        assertNotEquals(base, new MutableListOptions().setItemMarkerSuffixes(new String[] { "[ ]" }));
        assertNotEquals(base, new MutableListOptions().setParserEmulationFamily(ParserEmulationProfile.MARKDOWN));
    }

    @Test
    public void test_aCopyMadeThroughTheMutableViewEqualsTheOriginal() {
        ListOptions original = new MutableListOptions().setItemIndent(3).setEndOnDoubleBlank(true);

        assertEquals(original, original.getMutable());
        assertEquals(original, new MutableListOptions(original.setIn(new MutableDataSet())));
    }

    @Test
    public void test_itemInterruptWithTheSameFlagsAreEqual() {
        ListOptions.MutableItemInterrupt a = new ListOptions.MutableItemInterrupt().setBulletItemInterruptsParagraph(true);
        ListOptions.MutableItemInterrupt b = new ListOptions.MutableItemInterrupt().setBulletItemInterruptsParagraph(true);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new ListOptions.MutableItemInterrupt());
    }

    @Test
    public void test_aNumberedItemNotStartingAtOneInterruptsAParagraphOnlyWhenAllowed() {
        ListOptions.MutableItemInterrupt interrupt = new ListOptions.MutableItemInterrupt()
                .setOrderedItemInterruptsParagraph(true)
                .setOrderedNonOneItemInterruptsParagraph(false);

        assertTrue(interrupt.canInterrupt(true, true, false, false));
        assertFalse(interrupt.canInterrupt(true, false, false, false));
        interrupt.setOrderedNonOneItemInterruptsParagraph(true);
        assertTrue(interrupt.canInterrupt(true, false, false, false));
    }

    @Test
    public void test_anEmptyItemInterruptsOnlyWhenEmptyItemsAreAllowedToInterrupt() {
        ListOptions.MutableItemInterrupt interrupt = new ListOptions.MutableItemInterrupt()
                .setBulletItemInterruptsParagraph(true)
                .setEmptyBulletItemInterruptsParagraph(false)
                .setBulletItemInterruptsItemParagraph(true)
                .setEmptyBulletItemInterruptsItemParagraph(true);

        assertTrue(interrupt.canInterrupt(false, false, false, false));
        assertFalse(interrupt.canInterrupt(false, false, true, false));
        assertTrue(interrupt.canInterrupt(false, false, true, true));
    }

    @Test
    public void test_aSubListIsStartedByAnEmptyItemOnlyWhenEmptySubItemsAreAllowed() {
        ListOptions.MutableItemInterrupt interrupt = new ListOptions.MutableItemInterrupt()
                .setBulletItemInterruptsItemParagraph(true)
                .setEmptyBulletItemInterruptsItemParagraph(true)
                .setEmptyBulletSubItemInterruptsItemParagraph(false)
                .setOrderedItemInterruptsItemParagraph(true)
                .setOrderedNonOneItemInterruptsItemParagraph(false);

        assertTrue(interrupt.canStartSubList(false, false, false));
        assertFalse(interrupt.canStartSubList(false, false, true));
        assertTrue(interrupt.canStartSubList(true, true, false));
        assertFalse(interrupt.canStartSubList(true, false, false));
    }
}
