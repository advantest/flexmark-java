package com.vladsch.flexmark.util.ast;

import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class NodeTest {
    static class Leaf extends Node {
        Leaf(BasedSequence chars) {
            super(chars);
        }

        @NotNull
        @Override
        public BasedSequence[] getSegments() {
            return EMPTY_SEGMENTS;
        }
    }

    static class Marker extends Leaf {
        Marker(BasedSequence chars) {
            super(chars);
        }
    }

    static class Container extends Node {
        Container(BasedSequence chars) {
            super(chars);
        }

        @NotNull
        @Override
        public BasedSequence[] getSegments() {
            return EMPTY_SEGMENTS;
        }
    }

    static class BlockContainer extends Block {
        BlockContainer(BasedSequence chars) {
            super(chars);
        }

        @NotNull
        @Override
        public BasedSequence[] getSegments() {
            return EMPTY_SEGMENTS;
        }
    }

    static class Delimited extends Node {
        final BasedSequence open;
        final BasedSequence close;

        Delimited(BasedSequence open, BasedSequence close) {
            super();
            this.open = open;
            this.close = close;
        }

        @NotNull
        @Override
        public BasedSequence[] getSegments() {
            return new BasedSequence[] { open, close };
        }
    }

    private static final String TEXT = "line one\nline two\nline three";

    private BasedSequence text;
    private Document document;
    private Container outer;
    private Leaf one;
    private Marker two;
    private Leaf three;
    private Container inner;
    private Leaf deep;

    /*
     * document
     *   outer
     *     one
     *     two (Marker)
     *     three
     *     inner
     *       deep
     */
    @Before
    public void buildTree() {
        text = BasedSequence.of(TEXT);
        document = new Document(new MutableDataSet(), text);
        outer = new Container(text);
        one = new Leaf(text.subSequence(0, 4));
        two = new Marker(text.subSequence(5, 8));
        three = new Leaf(text.subSequence(9, 17));
        inner = new Container(text.subSequence(18, 28));
        deep = new Leaf(text.subSequence(18, 22));

        document.appendChild(outer);
        outer.appendChild(one);
        outer.appendChild(two);
        outer.appendChild(three);
        outer.appendChild(inner);
        inner.appendChild(deep);
    }

    private static List<Node> list(Iterable<Node> nodes) {
        List<Node> result = new ArrayList<>();
        for (Node node : nodes) result.add(node);
        return result;
    }

    @Test
    public void test_ancestorLookupFindsTheNearestMatchingType() {
        assertSame(inner, deep.getAncestorOfType(Container.class));
        assertSame(document, deep.getAncestorOfType(Document.class));
        assertSame(inner, deep.getAncestorOfType(Marker.class, Container.class));
        assertNull(deep.getAncestorOfType(Marker.class));
        assertNull(document.getAncestorOfType(Container.class));
    }

    @Test
    public void test_ancestorsOfTypeAreCountedOnceEach() {
        assertEquals(2, deep.countAncestorsOfType(Container.class));
        assertEquals(3, deep.countAncestorsOfType(Container.class, Document.class));
        assertEquals(0, deep.countAncestorsOfType(Marker.class));
        assertEquals(0, document.countAncestorsOfType(Container.class));
    }

    @Test
    public void test_directAncestorsStopAtTheFirstAncestorOfAnotherType() {
        assertEquals(2, deep.countDirectAncestorsOfType(null, Container.class));
        assertEquals(3, deep.countDirectAncestorsOfType(null, Container.class, Document.class));
        assertEquals(0, deep.countDirectAncestorsOfType(null, Document.class));
        assertEquals(0, two.countDirectAncestorsOfType(Leaf.class, Marker.class));
    }

    @Test
    public void test_directAncestorsCanSkipAnUncountedType() {
        assertEquals(1, deep.countDirectAncestorsOfType(Container.class, Document.class));
        assertEquals(1, deep.countDirectAncestorsOfType(Container.class, Marker.class, Document.class));
        assertEquals(0, deep.countDirectAncestorsOfType(Marker.class, Document.class));
    }

    @Test
    public void test_oldestAncestorIsTheOutermostOneBeforeAStopType() {
        assertSame(outer, deep.getOldestAncestorOfTypeAfter(Container.class, Document.class));
        assertNull(deep.getOldestAncestorOfTypeAfter(Document.class, Container.class));
        assertNull(deep.getOldestAncestorOfTypeAfter(Marker.class, Document.class));
    }

    @Test
    public void test_childAndDescendantTypeQueries() {
        assertSame(two, outer.getChildOfType(Marker.class));
        assertSame(inner, outer.getChildOfType(Container.class));
        assertNull(outer.getChildOfType(Document.class));
        assertNull(deep.getChildOfType(Leaf.class));

        assertTrue(deep.isOrDescendantOfType(Leaf.class));
        assertTrue(deep.isOrDescendantOfType(Document.class));
        assertFalse(deep.isOrDescendantOfType(Marker.class));
        assertEquals(1, deep.getNodeOfTypeIndex(Marker.class, Leaf.class));
        assertEquals(-1, deep.getNodeOfTypeIndex(Marker.class));
    }

    @Test
    public void test_childrenAreIterableInBothDirections() {
        assertEquals(java.util.Arrays.asList(one, two, three, inner), list(outer.getChildren()));
        assertEquals(java.util.Arrays.asList(inner, three, two, one), list(outer.getReversedChildren()));
        assertEquals(java.util.Arrays.asList(one, two, three, inner, deep), list(outer.getDescendants()));
        assertEquals(java.util.Arrays.asList(inner, deep, three, two, one), list(outer.getReversedDescendants()));
        assertSame(one, outer.getChildIterator().next());
        assertSame(inner, outer.getReversedChildIterator().next());
    }

    @Test
    public void test_aNodeWithoutChildrenHasEmptyIterationInEveryDirection() {
        assertFalse(one.getChildren().iterator().hasNext());
        assertFalse(one.getReversedChildren().iterator().hasNext());
        assertFalse(one.getDescendants().iterator().hasNext());
        assertFalse(one.getReversedDescendants().iterator().hasNext());
        assertFalse(one.getChildIterator().hasNext());
        assertFalse(one.getReversedChildIterator().hasNext());
        assertFalse(one.hasChildren());
    }

    @Test
    public void test_hasOrMoreChildrenCountsDirectChildrenOnly() {
        assertTrue(outer.hasChildren());
        assertTrue(outer.hasOrMoreChildren(1));
        assertTrue(outer.hasOrMoreChildren(4));
        assertFalse(outer.hasOrMoreChildren(5));
        assertFalse(one.hasOrMoreChildren(1));
    }

    @Test
    public void test_siblingNavigationSkipsOrSelectsNodesByType() {
        assertSame(two, one.getNext());
        assertSame(two, three.getPrevious());

        assertSame(three, two.getNextAnyNot(Marker.class));
        assertSame(inner, one.getNextAnyNot(Leaf.class));
        assertSame(two, one.getNextAny(Marker.class));
        assertNull(inner.getNextAny(Marker.class));
        assertSame(two, one.getNextAnyNot());
        assertSame(two, one.getNextAny());

        assertSame(one, two.getPreviousAnyNot(Marker.class));
        assertSame(two, three.getPreviousAny(Marker.class));
        assertNull(two.getPreviousAny(Container.class));
        assertSame(two, three.getPreviousAnyNot());
        assertSame(two, three.getPreviousAny());
    }

    @Test
    public void test_childSelectionSkipsOrSelectsNodesByType() {
        assertSame(inner, outer.getLastChildAny(Container.class));
        assertSame(three, outer.getLastChildAnyNot(Container.class));
        assertSame(two, outer.getFirstChildAny(Marker.class));
        assertSame(inner, outer.getFirstChildAnyNot(Leaf.class));
        assertNull(outer.getFirstChildAny(Document.class));
        assertNull(outer.getLastChildAny(Document.class));
        assertNull(outer.getLastChildAnyNot(Leaf.class, Container.class));
        assertSame(inner, outer.getLastChildAnyNot());
        assertSame(one, outer.getFirstChildAnyNot());
        assertSame(inner, outer.getLastChildAny());
        assertSame(one, outer.getFirstChildAny());
        assertSame(outer, deep.getGrandParent());
        assertNull(document.getGrandParent());
        assertNull(outer.getGrandParent());
    }

    @Test
    public void test_aChainIsTheRunOfSiblingsOfTheSameClass() {
        Marker second = new Marker(text.subSequence(9, 10));
        Marker third = new Marker(text.subSequence(10, 11));
        two.insertAfter(second);
        second.insertAfter(third);

        assertSame(two, third.getFirstInChain());
        assertSame(third, two.getLastInChain());
        assertSame(two, two.getFirstInChain());
        assertSame(third, third.getLastInChain());
        assertSame(one, one.getFirstInChain());
    }

    @Test
    public void test_extractingAChainMovesAllItsMembersToAnotherParent() {
        Marker second = new Marker(text.subSequence(9, 10));
        Marker third = new Marker(text.subSequence(10, 11));
        two.insertAfter(second);
        second.insertAfter(third);
        Container target = new Container(text);

        third.extractToFirstInChain(target);

        assertEquals(java.util.Arrays.asList(two, second, third), list(target.getChildren()));
        assertEquals(java.util.Arrays.asList(one, three, inner), list(outer.getChildren()));
    }

    @Test
    public void test_chainsOfNodesCanBeAppendedAndInserted() {
        Container target = new Container(text);
        Leaf anchor = new Leaf(text.subSequence(0, 1));
        target.appendChild(anchor);

        target.appendChain(two);
        assertEquals(java.util.Arrays.asList(anchor, two, three, inner), list(target.getChildren()));
        assertEquals(java.util.Arrays.asList(one), list(outer.getChildren()));

        Container other = new Container(text);
        Leaf otherAnchor = new Leaf(text.subSequence(0, 1));
        other.appendChild(otherAnchor);
        otherAnchor.insertChainAfter(two);
        assertEquals(java.util.Arrays.asList(otherAnchor, two, three, inner), list(other.getChildren()));

        Container third = new Container(text);
        Leaf thirdAnchor = new Leaf(text.subSequence(0, 1));
        third.appendChild(thirdAnchor);
        thirdAnchor.insertChainBefore(two);
        assertSame(two, third.getFirstChild());
        assertSame(thirdAnchor, third.getLastChild());
    }

    @Test
    public void test_prependingAndInsertingKeepParentEndsConsistent() {
        Leaf first = new Leaf(text.subSequence(0, 1));
        outer.prependChild(first);
        assertSame(first, outer.getFirstChild());
        assertSame(one, first.getNext());

        Leaf before = new Leaf(text.subSequence(0, 1));
        first.insertBefore(before);
        assertSame(before, outer.getFirstChild());

        Leaf after = new Leaf(text.subSequence(0, 1));
        inner.insertAfter(after);
        assertSame(after, outer.getLastChild());

        Container empty = new Container(text);
        Leaf only = new Leaf(text.subSequence(0, 1));
        empty.prependChild(only);
        assertSame(only, empty.getFirstChild());
        assertSame(only, empty.getLastChild());
    }

    @Test
    public void test_unlinkingFirstMiddleAndLastChildUpdatesTheParent() {
        two.unlink();
        assertEquals(java.util.Arrays.asList(one, three, inner), list(outer.getChildren()));
        assertNull(two.getParent());

        one.unlink();
        assertSame(three, outer.getFirstChild());

        inner.unlink();
        assertSame(three, outer.getLastChild());

        three.unlink();
        assertFalse(outer.hasChildren());
        assertNull(outer.getLastChild());
    }

    @Test
    public void test_removingChildrenDetachesThemAll() {
        outer.removeChildren();
        assertFalse(outer.hasChildren());
        assertNull(one.getParent());
        assertNull(one.getNext());
    }

    @Test
    public void test_takingChildrenOfANodeWithSeveralChildrenMovesTheWholeRun() {
        Container target = new Container(text);
        Leaf existing = new Leaf(text.subSequence(0, 1));
        target.appendChild(existing);

        target.takeChildren(outer);

        assertEquals(java.util.Arrays.asList(existing, one, two, three, inner), list(target.getChildren()));
        assertFalse(outer.hasChildren());
        assertSame(target, one.getParent());
        assertSame(target, inner.getParent());
    }

    @Test
    public void test_takingChildrenIntoAnEmptyNodeAdoptsThem() {
        Container target = new Container(text);
        target.takeChildren(outer);
        assertEquals(java.util.Arrays.asList(one, two, three, inner), list(target.getChildren()));
    }

    @Test
    public void test_takingASingleChildAppendsIt() {
        Container source = new Container(text);
        Leaf only = new Leaf(text.subSequence(0, 1));
        source.appendChild(only);
        Container target = new Container(text);
        Leaf existing = new Leaf(text.subSequence(0, 1));
        target.appendChild(existing);

        target.takeChildren(source);

        assertEquals(java.util.Arrays.asList(existing, only), list(target.getChildren()));
        assertFalse(source.hasChildren());
    }

    @Test
    public void test_takingChildrenOfANodeWithoutChildrenChangesNothing() {
        Container empty = new Container(text);
        outer.takeChildren(empty);
        assertEquals(4, list(outer.getChildren()).size());
    }

    @Test
    public void test_documentIsFoundFromAnyDepth() {
        assertSame(document, deep.getDocument());
        assertSame(document, document.getDocument());
    }

    @Test
    public void test_lineNumbersFollowTheSourceOffsets() {
        assertEquals(0, one.getLineNumber());
        assertEquals(0, one.getStartLineNumber());
        assertEquals(0, one.getEndLineNumber());
        assertEquals(1, three.getStartLineNumber());
        assertEquals(1, three.getEndLineNumber());
        assertEquals(2, inner.getStartLineNumber());
        assertEquals(2, inner.getEndLineNumber());
        assertEquals(0, outer.getStartLineNumber());
        assertEquals(2, document.getEndLineNumber());
    }

    @Test
    public void test_endLineOfAnEmptyNodeAtTheStartOfTheTextIsTheFirstLine() {
        Leaf empty = new Leaf(text.subSequence(0, 0));
        document.appendChild(empty);
        assertEquals(0, empty.getEndLineNumber());
    }

    @Test
    public void test_nodeFormsOfPositionQueries() {
        assertEquals(5, two.getStartOffset());
        assertEquals(8, two.getEndOffset());
        assertEquals(3, two.getTextLength());
        assertEquals(0, two.getStartOfLine());
        assertEquals(8, two.getEndOfLine());
        assertEquals(TEXT.length(), deep.getBaseSequence().length());
        assertEquals("[5, 8)", two.getSourceRange().toString());
    }

    @Test
    public void test_astStringNamesTheNodeAndShowsItsOffsets() {
        assertEquals("NodeTest$Leaf[0, 4]", one.toAstString(false));
        assertEquals("NodeTest$Marker[5, 8]", two.toAstString(true));
        assertEquals("NodeTest$Leaf{}", one.toString());
        assertEquals("NodeTest$Leaf", one.getNodeName());
    }

    @Test
    public void test_astCharsAbbreviateLongText() {
        StringBuilder out = new StringBuilder();
        Node.astChars(out, "short", "chars");
        assertEquals(" chars \"short\"", out.toString());

        out.setLength(0);
        Node.astChars(out, "0123456789abcdef", "chars");
        assertEquals(" chars \"01234" + Node.SPLICE + "bcdef\"", out.toString());

        out.setLength(0);
        Node.astChars(out, "", "chars");
        assertEquals("", out.toString());
    }

    @Test
    public void test_astExtraCharsAbbreviateLongTextAndHideEmptyText() {
        StringBuilder out = new StringBuilder();
        one.astExtraChars(out);
        assertEquals(" chars:[0, 4, \"line\"]", out.toString());

        out.setLength(0);
        new Leaf(text.subSequence(0, 17)).astExtraChars(out);
        assertTrue(out.toString().startsWith(" chars:[0, 17, \"line "));
        assertTrue(out.toString().contains(Node.SPLICE));
        assertTrue(out.toString().endsWith("e two\"]"));

        out.setLength(0);
        new Leaf(text.subSequence(3, 3)).astExtraChars(out);
        assertEquals("", out.toString());
    }

    @Test
    public void test_segmentSpanIsOmittedForANullSequence() {
        StringBuilder out = new StringBuilder();
        Node.segmentSpan(out, BasedSequence.NULL, "name");
        Node.segmentSpanChars(out, BasedSequence.NULL, "name");
        Node.segmentSpanCharsToVisible(out, BasedSequence.NULL, "name");
        assertEquals("", out.toString());
        assertEquals("", Node.toSegmentSpan(BasedSequence.NULL, "name"));
    }

    @Test
    public void test_segmentSpanShowsNameOffsetsAndEscapedChars() {
        assertEquals(" name:[0, 4]", Node.toSegmentSpan(text.subSequence(0, 4), "name"));
        assertEquals("[0, 4]", Node.toSegmentSpan(text.subSequence(0, 4), null));
        assertEquals("[0, 4]", Node.toSegmentSpan(text.subSequence(0, 4), "  "));

        StringBuilder out = new StringBuilder();
        Node.segmentSpanChars(out, text.subSequence(5, 13), "name");
        assertEquals(" name:[5, 13, \"one\\nline\"]", out.toString());
    }

    @Test
    public void test_visibleSegmentSpanAbbreviatesLongText() {
        StringBuilder out = new StringBuilder();
        Node.segmentSpanCharsToVisible(out, text.subSequence(0, 8), "name");
        assertEquals(" name:[0, 8, \"line one\"]", out.toString());

        out.setLength(0);
        Node.segmentSpanCharsToVisible(out, text.subSequence(0, 17), "name");
        assertTrue(out.toString().startsWith(" name:[0, 17, \"line "));
        assertTrue(out.toString().contains(Node.SPLICE));
    }

    @Test
    public void test_delimitedSegmentSpanShowsOpeningTextAndClosing() {
        StringBuilder out = new StringBuilder();
        Node.delimitedSegmentSpan(out, text.subSequence(0, 1), text.subSequence(1, 5), text.subSequence(5, 6), "Em");
        assertEquals(" EmOpen:[0, 1, \"l\"] Em:[1, 5, \"ine \"] EmClose:[5, 6, \"o\"]", out.toString());

        out.setLength(0);
        Node.delimitedSegmentSpan(out, text.subSequence(0, 1), text.subSequence(1, 17), text.subSequence(17, 18), "Em");
        assertTrue(out.toString().contains("Em:[1, 17, \"ine o"));
        assertTrue(out.toString().contains(Node.SPLICE));
    }

    @Test
    public void test_delimitedSegmentSpanCharsSkipsMissingParts() {
        StringBuilder out = new StringBuilder();
        Node.delimitedSegmentSpanChars(out, BasedSequence.NULL, text.subSequence(1, 5), BasedSequence.NULL, "Em");
        assertEquals(" Em:[1, 5, \"ine \"]", out.toString());

        out.setLength(0);
        Node.delimitedSegmentSpanChars(out, text.subSequence(0, 1), BasedSequence.NULL, text.subSequence(5, 6), "Em");
        assertEquals(" EmOpen:[0, 1, \"l\"] EmClose:[5, 6, \"o\"]", out.toString());

        out.setLength(0);
        Node.delimitedSegmentSpanChars(out, text.subSequence(0, 1), text.subSequence(1, 5), text.subSequence(5, 6), "Em");
        assertEquals(" EmOpen:[0, 1, \"l\"] Em:[1, 5, \"ine \"] EmClose:[5, 6, \"o\"]", out.toString());
    }

    @Test
    public void test_leadAndTrailSegmentsIgnoreNullSegments() {
        BasedSequence a = text.subSequence(0, 2);
        BasedSequence b = text.subSequence(4, 6);
        assertSame(a, Node.getLeadSegment(new BasedSequence[] { BasedSequence.NULL, a, b }));
        assertSame(b, Node.getTrailSegment(new BasedSequence[] { a, b, BasedSequence.NULL }));
        assertSame(BasedSequence.NULL, Node.getLeadSegment(new BasedSequence[] { BasedSequence.NULL }));
        assertSame(BasedSequence.NULL, Node.getTrailSegment(new BasedSequence[0]));
    }

    @Test
    public void test_spanningCharsCoverFirstToLastNonNullSegment() {
        BasedSequence a = text.subSequence(2, 4);
        BasedSequence b = text.subSequence(10, 12);
        BasedSequence c = text.subSequence(6, 8);
        assertEquals(text.subSequence(2, 12).toString(), Node.spanningChars(b, BasedSequence.NULL, a, c).toString());
        assertSame(BasedSequence.NULL, Node.spanningChars(BasedSequence.NULL, BasedSequence.NULL));
        assertSame(BasedSequence.NULL, Node.spanningChars());
    }

    @Test
    public void test_charsOfAContainerSpanItsChildren() {
        Container container = new Container(BasedSequence.NULL);
        container.appendChild(new Leaf(text.subSequence(5, 8)));
        container.appendChild(new Leaf(text.subSequence(10, 12)));
        container.setCharsFromContent();
        assertEquals(text.subSequence(5, 12).toString(), container.getChars().toString());
        assertEquals(text.subSequence(5, 12).toString(), container.getChildChars().toString());
        assertEquals(5, container.getChildChars().getStartOffset());
    }

    @Test
    public void test_charsFromContentNeverShrinkAlreadyAssignedChars() {
        Container container = new Container(text.subSequence(0, 28));
        container.appendChild(new Leaf(text.subSequence(5, 8)));
        container.setCharsFromContent();
        assertEquals(0, container.getStartOffset());
        assertEquals(28, container.getEndOffset());

        container.setCharsFromContentOnly();
        assertEquals(5, container.getStartOffset());
        assertEquals(8, container.getEndOffset());
    }

    @Test
    public void test_charsFromSegmentsOfDelimitedNodeSpanOpeningAndClosing() {
        Delimited node = new Delimited(text.subSequence(0, 2), text.subSequence(6, 8));
        node.setCharsFromContent();
        assertEquals(0, node.getStartOffset());
        assertEquals(8, node.getEndOffset());

        node.setCharsFromSegments();
        assertEquals("line", node.getChars().toString());
    }

    @Test
    public void test_charsFromSegmentsOfNodeWithoutSegmentsIsNull() {
        assertSame(BasedSequence.NULL, one.getCharsFromSegments());
    }

    @Test
    public void test_childCharsOfANodeWithoutChildrenAreNull() {
        assertSame(BasedSequence.NULL, one.getChildChars());
        assertSame(BasedSequence.NULL, one.getExactChildChars());
    }

    @Test
    public void test_exactChildCharsAreTheConcatenationOfTheChildren() {
        Container container = new Container(text);
        container.appendChild(new Leaf(text.subSequence(0, 4)));
        container.appendChild(new Leaf(text.subSequence(5, 8)));
        assertEquals("lineone", container.getExactChildChars().toString());
    }

    @Test
    public void test_trailingBlankLinesMoveOutOfTheirParent() {
        BlockContainer list = new BlockContainer(text);
        BlockContainer item = new BlockContainer(text.subSequence(0, 9));
        Leaf content = new Leaf(text.subSequence(0, 8));
        BlankLine blank = new BlankLine(text.subSequence(8, 9));
        document.appendChild(list);
        list.appendChild(item);
        item.appendChild(content);
        item.appendChild(blank);

        item.moveTrailingBlankLines();

        assertFalse(blank.getParent() == (Object) item);
        assertEquals(java.util.Arrays.asList(content), list(item.getChildren()));
    }

    @Test
    public void test_nodeWithoutTrailingBlankLineKeepsItsChildren() {
        outer.moveTrailingBlankLines();
        assertEquals(4, list(outer.getChildren()).size());
    }
}
