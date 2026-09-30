package com.vladsch.flexmark.ast.util;

import com.vladsch.flexmark.ast.ImageRef;
import com.vladsch.flexmark.ast.LinkRef;
import com.vladsch.flexmark.ast.RefNode;
import com.vladsch.flexmark.ast.Reference;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.KeepType;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeRepository;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.sequence.Escaping;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class ReferenceRepository extends NodeRepository<Reference> {

    final private boolean unicodeCaseFold;

    public ReferenceRepository(DataHolder options) {
        super(Parser.REFERENCES_KEEP.get(options));
        this.unicodeCaseFold = Parser.REFERENCE_LABEL_UNICODE_CASE_FOLD.get(options);
    }

    /**
     * @return true if keys are normalized with the Unicode case fold (CommonMark 0.30), false if they are only lower cased
     */
    public boolean isUnicodeCaseFold() {
        return unicodeCaseFold;
    }

    /**
     * Normalize the characters of a link label, including the delimiters, to a key of this repository
     *
     * @param label label characters with leading [ or ![ and trailing ] or ]:
     * @return normalized key
     */
    @NotNull
    public String normalizeKeyChars(@NotNull CharSequence label) {
        return Escaping.normalizeReferenceChars(label, true, unicodeCaseFold);
    }

    @NotNull
    @Override
    public DataKey<ReferenceRepository> getDataKey() {
        return Parser.REFERENCES;
    }

    @NotNull
    @Override
    public DataKey<KeepType> getKeepDataKey() {
        return Parser.REFERENCES_KEEP;
    }

    @NotNull
    @Override
    public String normalizeKey(@NotNull CharSequence key) {
        return Escaping.normalizeReference(key, true, unicodeCaseFold);
    }

    @NotNull
    @Override
    public Set<Reference> getReferencedElements(Node parent) {
        HashSet<Reference> references = new HashSet<>();
        visitNodes(parent, value -> {
            if (value instanceof RefNode) {
                Reference reference = ((RefNode) value).getReferenceNode(ReferenceRepository.this);
                if (reference != null) {
                    references.add(reference);
                }
            }
        }, LinkRef.class, ImageRef.class);
        return references;
    }
}
