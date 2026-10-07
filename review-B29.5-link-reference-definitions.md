# Review — B29.5: link reference definitions (CommonMark 0.29, examples 170, 184, 185)

Reviewed commits: `417041dad` *test(parser): pin link reference definition rules of CommonMark 0.29*,
`34ad7ef08` *feat(parser): require whitespace before link reference definition titles in CommonMark 0.29* and
`3a3b63932` *feat(parser): allow setext headings after link reference definitions in CommonMark 0.29*.
Review round 1 of max. 2. Reviewer: `code-review` sub-agent (Claude Sonnet 5.5).

**Result: no defects found; two test gaps closed.**

## Design

Three flags, all default `false` and set only by the `COMMONMARK_0_29` profile, as the rules are independent:

| Option                                        | Rule                                                        | Example  |
| --------------------------------------------- | ----------------------------------------------------------- | -------- |
| `REFERENCE_DEFINITION_TITLE_REQUIRES_SPACE`   | title must be separated from the destination by whitespace  | 170      |
| `HEADING_SETEXT_AFTER_REFERENCE_DEFINITIONS`  | definitions are not heading text; only-definitions is no heading | 184, 185 |
| `LINK_TITLE_PARENTHESES_NO_UNESCAPED_OPENING` | `(` in a parenthesized title must be escaped                | none     |

The third flag (#526) is normative 0.29 text but drives no spec example; it also applies to inline links.
Spec changes without implementation consequence: #454 (unused definition, already passed), #461 and #474 (wording).

Decision 9 is honoured: nothing is rewritten. The setext heading is found by a side effect free dry run of the
definition parser (`InlineParserImpl.getReferenceDefinitionsLength`); the definitions are still extracted by the
paragraph pre-processor in document order, so "first definition wins" is unchanged. When only part of the
paragraph is definitions, the paragraph keeps those lines and the heading takes the remaining lines, both
pointing at their original source ranges (pinned by a test).

## Confirmed correct

| Aspect                 | Finding                                                                               |
| ---------------------- | ------------------------------------------------------------------------------------- |
| Default preserves 0.28 | All keys false; 0.28 runs of adversarial inputs give the old AST and HTML.            |
| Line arithmetic        | Indented lines, tabs, CRLF, multi-line titles map correctly back to lines.            |
| Containers             | Block quotes and list items work; lazy continuation is identical in 0.28 and 0.29.    |
| Extensions             | Footnotes and abbreviations are block parsers or start with `[^` / `*[`; unaffected.  |
| `LINK_TITLE_STRING`    | Used by admonition and TOC extensions, deliberately left unchanged.                   |
| Formatter round-trip   | Definitions followed by a setext heading re-parse to the same structure.              |

## Applied

1. A test for a setext heading after definitions inside a block quote and a list item.

## Declined

Nothing. (A multi-line title test already existed; the reviewer's remark was covered.)

## Verification

- `LinkReferenceDefinitionTest`: 16 tests, 0 failures.
- `spec.0.29.known-failures.txt`: 2 entries left (282, 283); 170, 184, 185 removed.
- Full `mvn -o install`: BUILD SUCCESS.
