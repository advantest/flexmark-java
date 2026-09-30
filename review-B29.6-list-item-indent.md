# Review B29.6 - list items indented 4+ spaces (CommonMark 0.29, examples 282/283)

## Design

One flag, `Parser.LISTS_NO_ITEM_AT_CODE_INDENT` (mirrored in `ListOptions`/`MutableListOptions`), default `false`,
enabled only for `COMMONMARK_0_29` (in `setIn` via `getOptions()`). In `ListItemParser.tryContinue` (CommonMark
family) a marker line indented by at least the code indent, relative to the container content column, no longer
starts a sibling item. It falls through to paragraph continuation or an indented code block, which explains the
282 (continuation) versus 283 (code block, list ends) asymmetry from a single rule.

Spec: #497 corrected the tests (such lines are continuation lines or indented code); #543 removed the vestigial
"not separated by more than one blank line" wording. #543 is documentation-only, so no second flag.
Existing options (`CODE_INDENT`, `ITEM_INDENT`, `NEW_ITEM_CODE_INDENT`, `ITEMS_CAN_INTERRUPT_PARAGRAPH`) were
evaluated; only `CODE_INDENT` is reused, as the threshold.

Decision 9: nothing is rewritten; node sequences still point at their original source ranges.

## Reviewed

Code review (claude-sonnet-5.5), round 1: spec correctness, relative indent, no leak into other profiles,
tabs, block quotes, nested lists, extensions, formatter round trip, test quality.

Confirmed fine: relative indent, no leakage, tabs, extensions.

## Applied

| Finding                                                              | Action                                        |
|----------------------------------------------------------------------|-----------------------------------------------|
| Formatter indented every marker-like continuation line               | Only when source indent >= code indent        |
| Text inside `TextBase` was missed                                    | Walk up through `TextBase`                    |
| Indent emitted although soft break not kept                          | Only when the line break is actually emitted  |
| Missing tests (other family forced on, false positive, wrap mode)    | Added                                         |
| `ParserEmulationProfileTest` needs flag in `getOptions()`            | Added to `getOptions()`                       |

## Declined / known limitation

Example 283 cannot round-trip through the formatter: the indented code block after a list is re-emitted and is
absorbed by the last item. This is inherent to the formatter's list-item indentation; excluded from the
round-trip test with a comment. Extra tests for empty-item-plus-marker, wide `10.` spacing, fenced last child and
`endOnDoubleBlank` were not added: they exercise the unchanged parser path.