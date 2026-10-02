package com.vladsch.flexmark.util.collection;

import org.junit.Test;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OrderedSetSparseTest {
    private static OrderedSet<String> setOf(String... values) {
        OrderedSet<String> set = new OrderedSet<>();
        set.addAll(Arrays.asList(values));
        return set;
    }

    @Test
    public void test_removingAnElementLeavesAHoleAndKeepsTheOtherIndices() {
        OrderedSet<String> set = setOf("a", "b", "c");
        assertFalse(set.isSparse());

        assertTrue(set.remove("b"));

        assertTrue(set.isSparse());
        assertEquals(2, set.size());
        assertEquals(0, set.indexOf("a"));
        assertEquals(-1, set.indexOf("b"));
        assertEquals(2, set.indexOf("c"));
        assertFalse(set.isValidIndex(1));
        assertNull(set.getValueOrNull(1));
        assertEquals("c", set.getValue(2));
    }

    @Test
    public void test_validatingAHoleOrAnIndexOutOfRangeFails() {
        OrderedSet<String> set = setOf("a", "b", "c");
        set.remove("b");

        for (int index : new int[] { 1, 3, -1 }) {
            try {
                set.validateIndex(index);
                fail("index " + index + " must not be valid");
            } catch (IndexOutOfBoundsException expected) {
                // expected
            }
        }
        set.validateIndex(0);
        set.validateIndex(2);
    }

    @Test
    public void test_valuesOfASparseSetSkipTheHolesAndTheValueListDoesNot() {
        OrderedSet<String> set = setOf("a", "b", "c");
        set.remove("b");

        assertEquals(Arrays.asList("a", "c"), set.values());
        assertEquals(3, set.getValueList().size());
        assertArrayEquals(new Object[] { "a", "c" }, set.toArray());
        assertArrayEquals(new String[] { "a", "c" }, set.toArray(new String[0]));
    }

    @Test
    public void test_valuesOfADenseSetAreTheValueList() {
        OrderedSet<String> set = setOf("a", "b");

        assertEquals(Arrays.asList("a", "b"), set.values());
        assertArrayEquals(new Object[] { "a", "b" }, set.toArray());
    }

    @Test
    public void test_aValueCanBePlacedInAHole() {
        OrderedSet<String> set = setOf("a", "b", "c");
        set.remove("b");

        assertTrue(set.setValueAt(1, "x", null));

        assertEquals(1, set.indexOf("x"));
        assertFalse(set.isSparse());
    }

    @Test
    public void test_placingAnExistingValueAtItsOwnIndexChangesNothingAndAtAnotherIndexIsRejected() {
        OrderedSet<String> set = setOf("a", "b");

        assertFalse(set.setValueAt(1, "b", null));
        assertEquals(2, set.size());

        try {
            set.setValueAt(0, "b", null);
            fail("moving an element to another index must be rejected");
        } catch (IllegalStateException expected) {
            // expected
        }
    }

    @Test
    public void test_placingAValueAtAnOccupiedIndexIsRejected() {
        OrderedSet<String> set = setOf("a", "b");

        try {
            set.setValueAt(0, "x", null);
            fail("an occupied index must be rejected");
        } catch (IllegalStateException expected) {
            // expected
        }
        assertEquals("a", set.getValue(0));
    }

    @Test
    public void test_addNullReservesAnIndexWithoutAValue() {
        OrderedSet<String> set = setOf("a");

        set.addNull();
        set.add("c");

        assertEquals(2, set.size());
        assertEquals(2, set.indexOf("c"));
        assertFalse(set.isValidIndex(1));
        assertTrue(set.isSparse());
    }

    @Test
    public void test_aSetThatIsOnlyHoleFreeAtTheEndIsNotSparse() {
        OrderedSet<String> set = setOf("a", "b");

        set.remove("b");

        assertFalse(set.isSparse());
        assertEquals(Collections.singletonList("a"), set.values());
    }

    @Test
    public void test_indexBitSetMarksTheIndicesOfTheGivenItemsThatAreInTheSet() {
        OrderedSet<String> set = setOf("a", "b", "c", "d");

        BitSet bits = set.indexBitSet(Arrays.asList("d", "b", "zzz"));

        assertEquals(bitsOf(1, 3), bits);
    }

    @Test
    public void test_differenceBitSetMarksTheIndicesWhereTheSequenceDiffers() {
        OrderedSet<String> set = setOf("a", "b", "c", "d");

        assertEquals(new BitSet(), set.differenceBitSet(Arrays.asList("a", "b", "c", "d")));
        assertEquals(bitsOf(0, 1), set.differenceBitSet(Arrays.asList("b", "a", "c", "d")));
        assertEquals(bitsOf(0, 1, 2, 3), set.differenceBitSet(Arrays.asList("d", "c", "b", "a")));
    }

    @Test
    public void test_keyDifferenceBitSetComparesTheKeysOfEntries() {
        OrderedSet<String> set = setOf("a", "b", "c");

        BitSet bits = set.keyDifferenceBitSet(Arrays.asList(entry("a", 1), entry("c", 2), entry("b", 3)));

        assertEquals(bitsOf(1, 2), bits);
    }

    @Test
    public void test_valueDifferenceBitSetComparesTheValuesOfEntries() {
        OrderedSet<String> set = setOf("a", "b", "c");

        BitSet bits = set.valueDifferenceBitSet(Arrays.asList(entry(1, "a"), entry(2, "c"), entry(3, "b")));

        assertEquals(bitsOf(1, 2), bits);
        assertEquals(new BitSet(), set.valueDifferenceBitSet(Arrays.asList(entry(1, "a"), entry(2, "b"), entry(3, "c"))));
    }

    private static <K, V> AbstractMap.SimpleEntry<K, V> entry(K key, V value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    private static BitSet bitsOf(int... indices) {
        BitSet bits = new BitSet();
        for (int i : indices) bits.set(i);
        return bits;
    }
}
