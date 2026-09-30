# Review supplementary code points: emphasis delimiter flanking per code point

Scope: commits `467b0a4af..9519fac72` on `common-mark-updates`. `InlineParserImpl.scanDelimiters` now classifies the
character before and after a delimiter run as a whole Unicode code point. Reviewed by a `code-review` sub-agent
(one round; the reviewer read the code but did not build or run tests).

## Findings

| # | Finding                                                                                          | Outcome   |
|---|--------------------------------------------------------------------------------------------------|-----------|
| 1 | Bounds: `startIndex == 0` guard precedes `codePointBefore`; end of input is caught by `peek()`   | Confirmed |
| 2 | No other adjacent-character classification site; processors only consume the computed booleans   | Confirmed |
| 3 | Identical strings for BMP input, so behaviour-neutral                                            | Confirmed |
| 4 | Performance: same two `String` allocations as before, plus one `char[]` for supplementary only   | Confirmed |
| 5 | No positive `*` tests (opens after whitespace or punctuation)                                    | Applied   |
| 6 | Directional punctuation mode (`INLINE_DELIMITER_DIRECTIONAL_PUNCTUATIONS`) has no test           | Declined  |
| 7 | Letter tests pass before the fix as well                                                         | Noted     |

## Applied

* Added positive `*` tests for BMP and supplementary punctuation: after whitespace and between punctuation.

## Declined

* Directional mode (finding 6). The option is not defined by the CommonMark spec, so no expectation can be derived
  from it. A trial assertion showed even BMP punctuation renders differently there than in the default mode, so
  it would have been a guess. The code path is covered by the same fix and `ComboCoreDirectionalSpecTest` (BMP).

## Noted

* The letter tests are regression guards: a lone surrogate already behaves like a letter. Only the punctuation
  tests fail without the fix.
