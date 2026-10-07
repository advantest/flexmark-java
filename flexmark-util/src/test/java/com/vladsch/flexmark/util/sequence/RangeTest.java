package com.vladsch.flexmark.util.sequence;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class RangeTest {
    @Test
    public void test_nullRangeIsUniqueAndHasNoSpan() {
        assertSame(Range.NULL, Range.of(Integer.MAX_VALUE, Integer.MIN_VALUE));
        assertTrue(Range.NULL.isNull());
        assertFalse(Range.NULL.isNotNull());
        assertEquals(0, Range.NULL.getSpan());
        assertTrue(Range.of(3, 3).isNotNull());
    }

    @Test
    public void test_factoriesAndAccessors() {
        assertEquals(Range.of(2, 5), Range.ofLength(2, 3));
        assertEquals(Range.of(4, 4), Range.emptyOf(4));
        assertEquals(Range.EMPTY, Range.emptyOf(0));

        Range range = Range.of(2, 7);
        assertEquals(2, range.getStart());
        assertEquals(7, range.getEnd());
        assertEquals(2, range.component1());
        assertEquals(7, range.component2());
        assertEquals(2, range.getStartOffset());
        assertEquals(7, range.getEndOffset());
        assertEquals(5, range.getSpan());
        assertEquals("[2, 7)", range.toString());
    }

    @Test
    public void test_emptyAndNotEmpty() {
        assertTrue(Range.of(3, 3).isEmpty());
        assertTrue(Range.of(5, 3).isEmpty());
        assertFalse(Range.of(3, 3).isNotEmpty());
        assertTrue(Range.of(3, 4).isNotEmpty());
        assertFalse(Range.of(3, 4).isEmpty());
    }

    @Test
    public void test_derivedRangesReturnTheSameInstanceWhenNothingChanges() {
        Range range = Range.of(2, 7);
        assertSame(range, range.withStart(2));
        assertSame(range, range.withEnd(7));
        assertSame(range, range.withRange(2, 7));
        assertSame(range, range.startPlus(0));
        assertSame(range, range.startMinus(0));
        assertSame(range, range.endPlus(0));
        assertSame(range, range.endMinus(0));
        assertSame(range, range.shiftLeft(0));
        assertSame(range, range.shiftRight(0));
    }

    @Test
    public void test_derivedRangesMoveTheRequestedBoundary() {
        Range range = Range.of(2, 7);
        assertEquals(Range.of(3, 7), range.withStart(3));
        assertEquals(Range.of(2, 9), range.withEnd(9));
        assertEquals(Range.of(1, 4), range.withRange(1, 4));
        assertEquals(Range.of(4, 7), range.startPlus(2));
        assertEquals(Range.of(1, 7), range.startMinus(1));
        assertEquals(Range.of(2, 10), range.endPlus(3));
        assertEquals(Range.of(2, 6), range.endMinus(1));
        assertEquals(Range.of(0, 5), range.shiftLeft(2));
        assertEquals(Range.of(5, 10), range.shiftRight(3));
    }

    @Test
    public void test_containsIndexTreatsEndAsExclusive() {
        Range range = Range.of(2, 5);
        assertFalse(range.contains(1));
        assertTrue(range.contains(2));
        assertTrue(range.contains(4));
        assertFalse(range.contains(5));
        assertTrue(range.doesContain(2));
        assertFalse(range.doesContain(5));
    }

    @Test
    public void test_containsRange() {
        Range range = Range.of(2, 8);
        assertTrue(range.contains(Range.of(2, 8)));
        assertTrue(range.contains(Range.of(3, 5)));
        assertFalse(range.contains(Range.of(1, 5)));
        assertFalse(range.contains(Range.of(3, 9)));
        assertTrue(range.contains(3, 5));
        assertFalse(range.contains(1, 5));
        assertFalse(range.contains(3, 9));
        assertTrue(range.doesContain(Range.of(3, 5)));
        assertTrue(range.doesContain(3, 5));

        assertTrue(range.properlyContains(Range.of(3, 7)));
        assertFalse(range.properlyContains(Range.of(2, 7)));
        assertFalse(range.properlyContains(Range.of(3, 8)));
        assertTrue(range.doesProperlyContain(Range.of(3, 7)));
    }

    @Test
    public void test_containedBy() {
        Range range = Range.of(3, 5);
        assertTrue(range.isContainedBy(Range.of(3, 5)));
        assertTrue(range.isContainedBy(Range.of(2, 8)));
        assertFalse(range.isContainedBy(Range.of(4, 8)));
        assertFalse(range.isContainedBy(Range.of(2, 4)));
        assertTrue(range.isContainedBy(2, 8));
        assertFalse(range.isContainedBy(4, 8));

        assertTrue(range.isProperlyContainedBy(Range.of(2, 6)));
        assertFalse(range.isProperlyContainedBy(Range.of(3, 6)));
        assertFalse(range.isProperlyContainedBy(Range.of(2, 5)));
        assertTrue(range.isProperlyContainedBy(2, 6));
        assertFalse(range.isProperlyContainedBy(3, 6));
    }

    @Test
    public void test_overlapDoesNotIncludeTouchingRanges() {
        Range range = Range.of(4, 8);
        assertTrue(range.overlaps(Range.of(6, 10)));
        assertTrue(range.overlaps(Range.of(0, 5)));
        assertTrue(range.overlaps(Range.of(5, 6)));
        assertFalse(range.overlaps(Range.of(8, 10)));
        assertFalse(range.overlaps(Range.of(0, 4)));
        assertTrue(range.doesOverlap(Range.of(6, 10)));
        assertFalse(range.doesOverlap(Range.of(8, 10)));
        assertTrue(range.doesNotOverlap(Range.of(8, 10)));
        assertFalse(range.doesNotOverlap(Range.of(6, 10)));
    }

    @Test
    public void test_overlapOrAdjacentIncludesTouchingRanges() {
        Range range = Range.of(4, 8);
        assertTrue(range.overlapsOrAdjacent(Range.of(8, 10)));
        assertTrue(range.overlapsOrAdjacent(Range.of(0, 4)));
        assertTrue(range.overlapsOrAdjacent(Range.of(6, 10)));
        assertFalse(range.overlapsOrAdjacent(Range.of(9, 10)));
        assertFalse(range.overlapsOrAdjacent(Range.of(0, 3)));
        assertTrue(range.doesOverlapOrAdjacent(Range.of(8, 10)));
        assertFalse(range.doesOverlapOrAdjacent(Range.of(9, 10)));
        assertTrue(range.doesNotOverlapOrAdjacent(Range.of(9, 10)));
        assertTrue(range.doesNotOverlapNorAdjacent(Range.of(0, 3)));
        assertFalse(range.doesNotOverlapNorAdjacent(Range.of(8, 10)));
    }

    @Test
    public void test_adjacency() {
        Range range = Range.of(4, 8);
        assertTrue(range.isAdjacent(3));
        assertTrue(range.isAdjacent(8));
        assertFalse(range.isAdjacent(4));
        assertFalse(range.isAdjacent(7));
        assertTrue(range.isAdjacentAfter(3));
        assertFalse(range.isAdjacentAfter(8));
        assertTrue(range.isAdjacentBefore(8));
        assertFalse(range.isAdjacentBefore(3));

        assertTrue(range.isAdjacent(Range.of(0, 4)));
        assertTrue(range.isAdjacent(Range.of(8, 9)));
        assertFalse(range.isAdjacent(Range.of(9, 12)));
        assertTrue(range.isAdjacentBefore(Range.of(8, 9)));
        assertFalse(range.isAdjacentBefore(Range.of(0, 4)));
        assertTrue(range.isAdjacentAfter(Range.of(0, 4)));
        assertFalse(range.isAdjacentAfter(Range.of(8, 9)));
    }

    @Test
    public void test_positionTests() {
        Range range = Range.of(4, 8);
        assertTrue(range.isEqual(Range.of(4, 8)));
        assertFalse(range.isEqual(Range.of(4, 9)));
        assertFalse(range.isEqual(Range.of(3, 8)));

        assertTrue(range.isValidIndex(4));
        assertTrue(range.isValidIndex(8));
        assertFalse(range.isValidIndex(3));
        assertFalse(range.isValidIndex(9));

        assertTrue(range.isStart(4));
        assertFalse(range.isStart(5));
        assertTrue(range.isEnd(8));
        assertFalse(range.isEnd(7));

        assertTrue(range.isLast(7));
        assertFalse(range.isLast(8));
        assertFalse(range.isLast(3));

        assertTrue(range.leadBy(4));
        assertFalse(range.leadBy(5));
        assertTrue(range.leads(8));
        assertFalse(range.leads(7));
        assertTrue(range.trailedBy(9));
        assertFalse(range.trailedBy(7));
        assertTrue(range.trails(3));
        assertFalse(range.trails(5));
    }

    @Test
    public void test_intersectOfOverlappingRangesIsTheCommonPart() {
        assertEquals(Range.of(4, 6), Range.of(2, 6).intersect(Range.of(4, 9)));
        assertEquals(Range.of(4, 6), Range.of(2, 9).intersect(Range.of(4, 6)));
    }

    @Test
    public void test_intersectOfDisjointRangesIsEmptyAtTheEndOfTheOverlapCandidate() {
        Range result = Range.of(2, 4).intersect(Range.of(6, 9));
        assertTrue(result.isEmpty());
        assertEquals(Range.of(4, 4), result);
    }

    @Test
    public void test_intersectWithTheSameRangeReturnsTheInstance() {
        Range range = Range.of(2, 6);
        assertSame(range, range.intersect(Range.of(0, 10)));
    }

    @Test
    public void test_excludeCutsTheOverlappingEnd() {
        assertEquals(Range.of(2, 4), Range.of(2, 8).exclude(Range.of(4, 10)));
    }

    @Test
    public void test_excludeCutsTheOverlappingStart() {
        assertEquals(Range.of(5, 8), Range.of(2, 8).exclude(Range.of(0, 5)));
    }

    @Test
    public void test_excludeOfARangeInTheMiddleKeepsTheRangeBecauseItCannotBeSplit() {
        Range range = Range.of(2, 8);
        assertSame(range, range.exclude(Range.of(4, 5)));
    }

    @Test
    public void test_excludeOfAnOutsideRangeKeepsTheRange() {
        Range range = Range.of(2, 8);
        assertSame(range, range.exclude(Range.of(8, 12)));
        assertSame(range, range.exclude(Range.of(0, 2)));
    }

    @Test
    public void test_excludeOfACoveringRangeLeavesNothing() {
        assertEquals(Range.of(0, 0), Range.of(2, 8).exclude(Range.of(0, 10)));
    }

    @Test
    public void test_compareOrdersByStartThenByLongerRangeFirst() {
        assertEquals(-1, Range.of(1, 5).compare(Range.of(2, 5)));
        assertEquals(1, Range.of(3, 5).compare(Range.of(2, 5)));
        assertEquals(-1, Range.of(2, 9).compare(Range.of(2, 5)));
        assertEquals(1, Range.of(2, 4).compare(Range.of(2, 5)));
        assertEquals(0, Range.of(2, 5).compare(Range.of(2, 5)));
    }

    @Test
    public void test_includeOfNullRangeKeepsTheOtherOperand() {
        Range range = Range.of(2, 5);
        assertSame(range, range.include(Range.NULL));
        assertSame(Range.NULL, Range.NULL.include(Range.NULL));
        assertEquals(range, Range.NULL.include(range));
    }

    @Test
    public void test_includeExpandsToCoverBoth() {
        assertEquals(Range.of(1, 9), Range.of(2, 5).include(Range.of(1, 9)));
        assertEquals(Range.of(2, 9), Range.of(2, 5).include(Range.of(7, 9)));
        assertEquals(Range.of(0, 5), Range.of(2, 5).include(0));
        assertEquals(Range.of(2, 8), Range.of(2, 5).include(8));
        assertEquals(Range.of(1, 7), Range.of(2, 5).include(1, 7));
        assertEquals(Range.of(1, 7), Range.NULL.include(1, 7));
        assertEquals(Range.of(1, 7), Range.of(2, 5).expandToInclude(Range.of(1, 7)));
    }

    @Test
    public void test_basedSubSequenceExtractsTheRange() {
        assertEquals("cde", Range.of(2, 5).basedSubSequence("abcdefg").toString());
        assertEquals("cde", Range.of(2, 5).subSequence("abcdefg").toString());
        assertEquals("cde", Range.of(2, 5).charSubSequence("abcdefg").toString());
        assertEquals("cde", Range.of(2, 5).richSubSequence("abcdefg").toString());
    }

    @Test
    public void test_safeSubSequenceClampsTheRangeToTheText() {
        String text = "abcdefg";
        assertEquals("fg", Range.of(5, 20).basedSafeSubSequence(text).toString());
        assertEquals("abc", Range.of(-3, 3).basedSafeSubSequence(text).toString());
        assertEquals("", Range.of(10, 20).basedSafeSubSequence(text).toString());
        assertSame(BasedSequence.NULL, Range.NULL.basedSafeSubSequence(text));

        assertEquals("fg", Range.of(5, 20).richSafeSubSequence(text).toString());
        assertEquals("abc", Range.of(-3, 3).richSafeSubSequence(text).toString());
        assertSame(RichSequence.NULL, Range.NULL.richSafeSubSequence(text));

        assertEquals("fg", Range.of(5, 20).safeSubSequence(text).toString());
        assertEquals("abc", Range.of(-3, 3).safeSubSequence(text).toString());
        assertEquals("", Range.NULL.safeSubSequence(text).toString());
    }

    @Test
    public void test_equalityAndHashCodeDependOnBothBoundaries() {
        assertEquals(Range.of(1, 2), Range.of(1, 2));
        assertEquals(Range.of(1, 2).hashCode(), Range.of(1, 2).hashCode());
        assertNotEquals(Range.of(1, 2), Range.of(1, 3));
        assertNotEquals(Range.of(1, 2), Range.of(0, 2));
        assertNotEquals(Range.of(1, 2), "[1, 2)");
        Range range = Range.of(1, 2);
        assertEquals(range, range);
    }
}
