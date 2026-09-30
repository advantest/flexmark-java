# Review — B29.2: code spans (CommonMark 0.29, examples 108, 331-337, 637)

Reviewed commits: `d88e9d1c0` *test(parser): pin code span normalization rules of CommonMark 0.29* and
`df0ac4fcd` *feat(parser): normalize code spans the CommonMark 0.29 way*.
Review round 1 of max. 2. Reviewer: `code-review` sub-agent (Claude Sonnet 5.5).

**Result: one real defect found in the change, one out-of-scope observation.**

## Design

One flag, `Parser.CODE_SPAN_NORMALIZE_LINE_ENDINGS_AND_KEEP_INTERIOR_SPACES` (default `false`, set by the
`COMMONMARK_0_29` profile only). The three changelog items are one normalization rule and cannot be
enabled separately in a meaningful way. The parser is unchanged: the `Code` node keeps its raw source text,
so source positions are untouched. `CoreNodeRenderer` normalizes on output.

## Confirmed correct

| Aspect                 | Finding                                                                                  |
| ---------------------- | ---------------------------------------------------------------------------------------- |
| Default preserves 0.28 | With the option off, the old code path is executed unchanged.                            |
| Profile wiring         | Set only in the `COMMONMARK_0_29` branch; `COMMONMARK_LATEST` untouched.                 |
| Formatter              | `CoreNodeFormatter` writes raw text; a round-trip test was added and passes.             |
| docx converter         | Renders children raw, no whitespace collapsing, no change needed.                        |

## Applied

1. **Trailing line ending lost with `CODE_SOFT_LINE_BREAKS` in 0.29.** The parser adds no soft break child for
   a trailing line ending, so `` `a\n` `` rendered `a` instead of `a ` and `` `\n` `` rendered nothing.
   The renderer now emits the missing space unless it is the stripped one. Tests added first
   and observed failing for this reason.

## Declined

| Suggestion                                                    | Reason                                                                  |
| ------------------------------------------------------------- | ----------------------------------------------------------------------- |
| Apply the normalization in the Jira and YouTrack converters   | They emit other markup, not CommonMark HTML; out of scope for this cluster. They keep their current behaviour in every profile. |

## Verification

- `CodeSpanNormalizationTest`: 14 tests, 0 failures; `ComboOrigSpec029CoreTest` and `FullOrigSpec*CoreTest` green.
- Full `mvn -o install`: BUILD SUCCESS.
