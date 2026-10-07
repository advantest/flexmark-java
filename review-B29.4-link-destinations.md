# Review — B29.4: link destinations in pointy brackets (CommonMark 0.29, examples 486, 490, 491)

Reviewed commits: `afda9b298` *test(parser): pin link destination rules in pointy brackets of CommonMark 0.29*,
`c585cb5e0` *feat(parser): allow spaces in pointy bracket link destinations of CommonMark 0.29* and
`a6efa5f64` *fix(formatter): do not emit the url opening marker of an image twice*.
Review round 1 of max. 2. Reviewer: `code-review` sub-agent (Claude Sonnet 5.5).

**Result: no defects found; one test gap closed.**

## Design

Two flags, both default `false` and set only by the `COMMONMARK_0_29` profile, as the rules are independent:

| Option                                              | Rule                                                              |
| --------------------------------------------------- | ----------------------------------------------------------------- |
| `LINK_DESTINATION_POINTY_BRACKETS_ALLOW_SPACES`     | `<...>` may contain spaces and tabs; `\>` does not close it       |
| `LINK_DESTINATION_NOT_STARTING_WITH_POINTY_BRACKET` | a destination without pointy brackets may not start with `<`      |

`SPACE_IN_LINK_URLS` is not reused: it also allows spaces in bare destinations and refuses a space before a quote,
neither of which is 0.29. The new flag takes precedence over it for `<...>`.

Decision 9 is honoured: only the matched source range changes. The `<` and `>` remain the url opening and closing
markers of the node and the url keeps its offsets (pinned by a test).

## Confirmed correct

| Aspect                 | Finding                                                                                 |
| ---------------------- | --------------------------------------------------------------------------------------- |
| Default preserves 0.28 | Pattern choice and the `<` check are skipped when the flags are `false`.                |
| Reference definitions  | Share `parseLinkDestination`, so example 164 (`<my url>`) now passes; its entry was removed. |
| Formatter fix          | Only the image branch emitted the opening marker twice; a round-trip test covers it.    |

## Applied

1. Tests for a tab inside `<...>` and for an empty `<>` (reviewer noted the gap; no bug).

## Declined

Nothing.

## Verification

- `LinkDestinationPointyBracketsTest`: 11 tests, 0 failures.
- `spec.0.29.known-failures.txt`: 5 entries left (170, 184, 185, 282, 283); 486, 490, 491 and 164 removed.
- Full `mvn -o install`: BUILD SUCCESS.
