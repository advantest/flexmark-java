package com.vladsch.flexmark.util.sequence;

import com.vladsch.flexmark.util.misc.Pair;
import com.vladsch.flexmark.util.misc.Pair;
import org.junit.Test;

import static com.vladsch.flexmark.util.misc.CharPredicate.SPACE;
import static com.vladsch.flexmark.util.misc.CharPredicate.SPACE_TAB;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SequenceUtilsTest {

    @Test
    public void test_lastIndexOfAnyNot() {
        assertEquals(15, SequenceUtils.lastIndexOfAnyNot("0123456789  2345", SPACE, 0, 17));
        assertEquals(15, SequenceUtils.lastIndexOfAnyNot("0123456789  2345", SPACE, 0, 16));
        assertEquals(15, SequenceUtils.lastIndexOfAnyNot("0123456789  2344", SPACE, 0, 15));
        assertEquals(14, SequenceUtils.lastIndexOfAnyNot("0123456789  2343", SPACE, 0, 14));
        assertEquals(13, SequenceUtils.lastIndexOfAnyNot("0123456789  2342", SPACE, 0, 13));
        assertEquals(12, SequenceUtils.lastIndexOfAnyNot("0123456789  2342", SPACE, 0, 12));
        assertEquals(9, SequenceUtils.lastIndexOfAnyNot("0123456789  2342", SPACE, 0, 11));
    }

    @Test
    public void test_countTrailing() {
        assertEquals(0, SequenceUtils.countTrailing("0123456789  2345", SPACE, 0, 17));
        assertEquals(0, SequenceUtils.countTrailing("0123456789  2345", SPACE, 0, 16));
        assertEquals(0, SequenceUtils.countTrailing("0123456789  2344", SPACE, 0, 15));
        assertEquals(0, SequenceUtils.countTrailing("0123456789  2343", SPACE, 0, 14));
        assertEquals(0, SequenceUtils.countTrailing("0123456789  2342", SPACE, 0, 13));
        assertEquals(2, SequenceUtils.countTrailing("0123456789  2342", SPACE, 0, 12));
        assertEquals(1, SequenceUtils.countTrailing("0123456789  2342", SPACE, 0, 11));
        assertEquals(0, SequenceUtils.countTrailing("0123456789  2342", SPACE, 0, 10));
    }

    @Test
    public void test_subSequenceBeforeAndAfterARange() {
        assertEquals("ab", SequenceUtils.subSequenceBefore("abcdef", Range.of(2, 4)));
        assertEquals("ef", SequenceUtils.subSequenceAfter("abcdef", Range.of(2, 4)));
        assertEquals("cdef", SequenceUtils.subSequence("abcdef", 2));
        assertEquals("abcdef", SequenceUtils.subSequence("abcdef", Range.NULL));
        assertEquals("cd", SequenceUtils.subSequence("abcdef", Range.of(2, 4)));

        Pair<String, String> around = SequenceUtils.subSequenceBeforeAfter("abcdef", Range.of(2, 4));
        assertEquals("ab", around.getFirst());
        assertEquals("ef", around.getSecond());
    }

    @Test
    public void test_subSequenceBeforeAndAfterTheNullRangeIsNull() {
        assertNull(SequenceUtils.subSequenceBefore("abcdef", Range.NULL));
        assertNull(SequenceUtils.subSequenceAfter("abcdef", Range.NULL));
        Pair<String, String> around = SequenceUtils.subSequenceBeforeAfter("abcdef", Range.NULL);
        assertNull(around.getFirst());
        assertNull(around.getSecond());
    }

    @Test
    public void test_indexOfNotSkipsRunOfTheSameCharacter() {
        assertEquals(3, SequenceUtils.indexOfNot("aaabaa", 'a'));
        assertEquals(3, SequenceUtils.indexOfNot("aaabaa", 'a', 1));
        assertEquals(4, SequenceUtils.indexOfNot("aaabaa", 'b', 3, 10));
        assertEquals(-1, SequenceUtils.indexOfNot("aaaa", 'a'));
        assertEquals(-1, SequenceUtils.indexOfNot("aaabaa", 'a', 0, 3));
        assertEquals(3, SequenceUtils.indexOfNot("aaabaa", 'a', -5, 10));
    }

    @Test
    public void test_lastIndexOfNotSkipsTrailingRunOfTheSameCharacter() {
        assertEquals(3, SequenceUtils.lastIndexOfNot("aaabaa", 'a', 0, 10));
        assertEquals(3, SequenceUtils.lastIndexOfNot("aaabaa", 'a', 0, 5));
        assertEquals(2, SequenceUtils.lastIndexOfNot("aaabaa", 'b', 0, 3));
        assertEquals(-1, SequenceUtils.lastIndexOfNot("aaaa", 'a', 0, 10));
        assertEquals(-1, SequenceUtils.lastIndexOfNot("aaabaa", 'a', 4, 10));
        assertEquals(-1, SequenceUtils.lastIndexOfNot("aaabaa", 'a', -3, 2));
    }

    @Test
    public void test_containsAnyNotFindsACharacterOutsideTheSet() {
        assertTrue(SequenceUtils.containsAnyNot("  a ", SPACE));
        assertFalse(SequenceUtils.containsAnyNot("    ", SPACE));
        assertFalse(SequenceUtils.containsAnyNot("  a ", SPACE, 3));
        assertTrue(SequenceUtils.containsAnyNot("  a ", SPACE, 1));
        assertFalse(SequenceUtils.containsAnyNot("  a ", SPACE, 0, 2));
        assertTrue(SequenceUtils.containsAnyNot("  a ", SPACE, 0, 3));
        assertTrue(SequenceUtils.containsAny("  a ", SPACE, 3));
        assertFalse(SequenceUtils.containsAny("  a", SPACE, 2));
    }

    @Test
    public void test_matchesOfWholeSequenceCanIgnoreCase() {
        assertTrue(SequenceUtils.matches("Hello", "Hello", false));
        assertFalse(SequenceUtils.matches("Hello", "hello", false));
        assertTrue(SequenceUtils.matches("Hello", "hELLO", true));
        assertTrue(SequenceUtils.matchesIgnoreCase("Hello", "hELLO"));
        assertFalse(SequenceUtils.matchesIgnoreCase("Hello", "hELL"));
        assertFalse(SequenceUtils.matchesIgnoreCase("Hello", "hELLOO"));
        assertFalse(SequenceUtils.matchesIgnoreCase("Hello", "hELLA"));
    }

    @Test
    public void test_matchCharsAtAnOffsetCanIgnoreCase() {
        assertTrue(SequenceUtils.matchChars("say Hello", "hello", 4, true));
        assertTrue(SequenceUtils.matchCharsIgnoreCase("say Hello", "HELLO", 4));
        assertFalse(SequenceUtils.matchCharsIgnoreCase("say Hello", "HELLO", 3));
        assertTrue(SequenceUtils.matchChars("Hello there", "Hello", true));
        assertTrue(SequenceUtils.matchCharsIgnoreCase("Hello there", "hELLO"));
        assertFalse(SequenceUtils.matchChars("Hello there", "hello", false));
        assertFalse(SequenceUtils.matchChars("Hello", "Hello there", 0, true));
    }

    @Test
    public void test_matchCharsReversedAreAnchoredAtTheEndIndex() {
        assertTrue(SequenceUtils.matchCharsReversed("say Hello", "Hello", 8));
        assertFalse(SequenceUtils.matchCharsReversed("say Hello", "Hello", 7));
        assertFalse(SequenceUtils.matchCharsReversed("say Hello", "hello", 8));
        assertTrue(SequenceUtils.matchCharsReversed("say Hello", "hello", 8, true));
        assertTrue(SequenceUtils.matchCharsReversedIgnoreCase("say Hello", "HELLO", 8));
        assertFalse(SequenceUtils.matchCharsReversedIgnoreCase("say Hello", "HELLO", 7));
        assertFalse(SequenceUtils.matchCharsReversed("Hi", "Hello", 1));
        assertFalse(SequenceUtils.matchCharsReversedIgnoreCase("Hi", "Hello", 1));
    }

    @Test
    public void test_matchedCharCountStopsAtTheFirstDifference() {
        assertEquals(3, SequenceUtils.matchedCharCount("abcdef", "abcxyz", 0, 10, false, false));
        assertEquals(6, SequenceUtils.matchedCharCount("abcdef", "abcdef", 0, 10, false, false));
        assertEquals(2, SequenceUtils.matchedCharCount("xxabcdef", "abXX", 2, 10, false, false));
        assertEquals(4, SequenceUtils.matchedCharCount("abcdef", "abcdef", 0, 4, false, false));
        assertEquals(0, SequenceUtils.matchedCharCount("abcdef", "abcdefgh", 0, 10, true, false));
        assertEquals(6, SequenceUtils.matchedCharCount("abcdef", "abcdef", 0, 10, true, false));
    }

    @Test
    public void test_matchedCharCountCanIgnoreCase() {
        assertEquals(3, SequenceUtils.matchedCharCount("abcdef", "ABCXYZ", 0, 10, false, true));
        assertEquals(6, SequenceUtils.matchedCharCount("abcdef", "ABCDEF", 0, 10, false, true));
        assertEquals(0, SequenceUtils.matchedCharCount("abcdef", "ABCDEF", 0, 10, false, false));
        assertEquals(0, SequenceUtils.matchedCharCount("abcdef", "xBCDEF", 0, 10, false, true));
    }

    @Test
    public void test_matchedCharCountReversedCountsFromTheEnd() {
        assertEquals(3, SequenceUtils.matchedCharCountReversed("xyzabc", "xxxabc", 0, 6, false));
        assertEquals(6, SequenceUtils.matchedCharCountReversed("abcabc", "abcabc", 0, 6, false));
        assertEquals(3, SequenceUtils.matchedCharCountReversed("xyzabc", "xxxabc", 0, 6, true));
        assertEquals(6, SequenceUtils.matchedCharCountReversed("ABCDEF", "abcdef", 0, 6, true));
        assertEquals(0, SequenceUtils.matchedCharCountReversed("ABCDEF", "abcdef", 0, 6, false));
        assertEquals(2, SequenceUtils.matchedCharCountReversed("abcdef", "xxxxef", 0, 100, true));
        assertEquals(3, SequenceUtils.matchedCharCountReversed("abcdef", "def", 0, 6, false));
        assertEquals(2, SequenceUtils.matchedCharCountReversed("abcxef", "def", 0, 6, false));
    }

    @Test
    public void test_compareHandlesMissingSequences() {
        assertEquals(0, SequenceUtils.compare(null, null, false));
        assertEquals(-1, SequenceUtils.compare(null, "a", false));
        assertEquals(1, SequenceUtils.compare("a", null, false));
        assertEquals(0, SequenceUtils.compare(null, null, true, null));
    }

    @Test
    public void test_compareOrdersByFirstDifferenceThenByLength() {
        assertTrue(SequenceUtils.compare("abc", "abd", false) < 0);
        assertTrue(SequenceUtils.compare("abd", "abc", false) > 0);
        assertTrue(SequenceUtils.compare("ab", "abc", false) < 0);
        assertTrue(SequenceUtils.compare("abc", "ab", false) > 0);
        assertEquals(0, SequenceUtils.compare("abc", "abc", false));
    }

    @Test
    public void test_compareIgnoringCaseTreatsDifferentCaseAsEqual() {
        assertEquals(0, SequenceUtils.compare("ABC", "abc", true));
        assertTrue(SequenceUtils.compare("ABC", "abc", false) < 0);
        assertTrue(SequenceUtils.compare("ABC", "abd", true) < 0);
    }

    @Test
    public void test_compareCanIgnoreCharactersFromASet() {
        assertEquals(0, SequenceUtils.compare("a b", "a\tb", false, SPACE_TAB));
        assertTrue(SequenceUtils.compare("a b", "a-b", false, SPACE_TAB) < 0);
        assertEquals(0, SequenceUtils.compare("A b", "a\tB", true, SPACE_TAB));
        assertTrue(SequenceUtils.compare("A b", "a-B", true, SPACE_TAB) < 0);
        assertTrue(SequenceUtils.compare("a b", "a\tb", false, null) > 0);
    }

    @Test
    public void test_indexOfAllFindsEveryNonOverlappingOccurrence() {
        assertArrayEquals(new int[] { 0, 3, 6 }, SequenceUtils.indexOfAll("abcabcabc", "abc"));
        assertArrayEquals(new int[] { 1, 5 }, SequenceUtils.indexOfAll("xabxxabx", "ab"));
        assertArrayEquals(SequenceUtils.EMPTY_INDICES, SequenceUtils.indexOfAll("abcabc", "xyz"));
        assertArrayEquals(SequenceUtils.EMPTY_INDICES, SequenceUtils.indexOfAll("abcabc", ""));
    }

    @Test
    public void test_indexOfAllGrowsPastTheInitialCapacity() {
        StringBuilder sb = new StringBuilder();
        int[] expected = new int[40];
        for (int i = 0; i < expected.length; i++) {
            expected[i] = sb.length();
            sb.append("ab,");
        }
        assertArrayEquals(expected, SequenceUtils.indexOfAll(sb, "ab"));
    }

    @Test
    public void test_lastIndexOfSequenceFindsTheRightmostOccurrence() {
        assertEquals(6, SequenceUtils.lastIndexOf("abcabcabc", "abc", 0, 9));
        assertEquals(3, SequenceUtils.lastIndexOf("abcabcabc", "abc", 0, 7));
        assertEquals(6, SequenceUtils.lastIndexOf("abcabcabc", "abc", 4, 9));
        assertEquals(-1, SequenceUtils.lastIndexOf("abcabc", "xyz", 0, 6));
        assertEquals(0, SequenceUtils.lastIndexOf("abcabc", "", 0, 6));
    }

    @Test
    public void test_indexOfSequenceWithEmptyNeedleReturnsTheStart() {
        assertEquals(2, SequenceUtils.indexOf("abcabc", "", 2, 6));
        assertEquals(0, SequenceUtils.indexOf("abcabc", "", -2, 6));
        assertEquals(3, SequenceUtils.indexOf("abcabc", "abc", 1, 6));
        assertEquals(-1, SequenceUtils.indexOf("abcabc", "abc", 1, 5));
    }

    @Test
    public void test_indexOfFindsAMatchEndingAtTheLastPositionOfTheWindow() {
        assertEquals(3, SequenceUtils.indexOf("hello", "lo"));
        assertEquals(1, SequenceUtils.indexOf("aab", "ab"));
        assertEquals(2, SequenceUtils.indexOf("aaab", "ab"));
        assertEquals(4, SequenceUtils.indexOf("abcabc", "bc", 2, 6));
        assertEquals(-1, SequenceUtils.indexOf("abcabc", "bc", 2, 5));
    }
}