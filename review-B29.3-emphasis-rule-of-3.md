# Review — B29.3: emphasis, the rule of three (CommonMark 0.29, examples 415, 416)

Reviewed commits: `58478ead1` *test(parser): pin the rule of three exception of CommonMark 0.29* and
`a9f486b93` *feat(parser): apply the rule of three exception of CommonMark 0.29*.
Review round 1 of max. 2. Reviewer: `code-review` sub-agent (Claude Sonnet 5.5).

**Result: one real defect found in the change, fixed.**

## Design

One flag, `Parser.EMPHASIS_MULTIPLE_OF_THREE_EXEMPTION` (default `false`, set by the `COMMONMARK_0_29` profile
only). `EmphasisDelimiterProcessor` skips the "multiple of 3" veto when both run lengths are multiples of 3,
as in rules 9 and 10 of the 0.29 spec ("... unless both lengths are multiples of 3").
The AST changes, so this is a parser change; rendering and formatting are untouched.

## Confirmed correct

| Aspect                 | Finding                                                                               |
| ---------------------- | ------------------------------------------------------------------------------------- |
| Default preserves 0.28 | Default `false` reduces the condition to the old one; `COMMONMARK_LATEST` untouched.  |
| Extensions             | Strikethrough, subscript etc. do not subclass `EmphasisDelimiterProcessor`.           |
| Old constructor        | The two-argument constructors are kept for source compatibility.                      |

## Applied

1. **The rule used the length left in a run, not its original length.** `DelimiterRun.length()` shrinks as a run
   is partly used; the spec and commonmark.js use the length as scanned. `**a***b**c**` differed from
   commonmark.js 0.29.0. Test added first and observed failing, then `DelimiterRun.originalLength()` added
   (default method) and used only when the flag is set.

## Declined

Nothing.

## Verification

- `EmphasisRuleOfThreeTest`: 6 tests, 0 failures; `ComboOrigSpec029CoreTest` and `FullOrigSpec*CoreTest` green.
- `spec.0.29.known-failures.txt`: 9 entries left, 415 and 416 removed.
- Full `mvn -o install`: BUILD SUCCESS.
