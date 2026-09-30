# Review B30: CommonMark 0.30 compliance and default

Scope: commits `dabddd19a..HEAD` on `common-mark-updates`. Four options (`<textarea>` type 1 block, label case
fold, HTML declarations, VT/FF in tags) plus the `COMMONMARK_0_30` profile as the default. Reviewed by a
`code-review` sub-agent, two rounds: after Part 3 (parser changes) and after Part 4 (default switch).

## Findings

| # | Round | Finding                                                                                  | Outcome   |
|---|-------|------------------------------------------------------------------------------------------|-----------|
| 1 | 1     | Gating exhaustive, `\s` narrowing correct, `HTML_TAG` cache key distinguishes variants   | Confirmed |
| 2 | 1     | Case fold applied to the lookup key only, raw label text untouched                       | Confirmed |
| 3 | 2     | `COMMONMARK_0_30` wiring mirrors 0.29; older profiles and families opt out of all keys   | Confirmed |
| 4 | 2     | Nine 0.29 key javadocs in `Parser.java` said "every other profile", now inexact          | Applied   |
| 5 | 2     | Plan referenced a not yet existing `review-B30.md`                                       | Applied   |

## Applied

* The nine 0.29 key javadocs now say "every profile other than COMMONMARK_0_29 and COMMONMARK_0_30".
* This file was added.

## Declined

Nothing declined.

## Not verified

* The case fold is an approximation (lower, upper, lower per code point), not the Unicode `CaseFolding.txt`.
  163 of 1606 C and F entries differ from the latest table (U+0130, Cherokee, characters newer than the JDK).
* Formatter: no fix was needed; round trips are tested for `<textarea>` and VT/FF only.
