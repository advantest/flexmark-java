# Review of Task F1 (Unicode full case folding)

## What changed

* Vendored `CaseFolding.txt` 15.0.0 verbatim (resource of `flexmark-util-sequence`), Unicode License v3 text in
  `licenses/UNICODE-LICENSE-V3.txt`, notices in `LICENSE.txt` and `README.md`.
* `Escaping.caseFold` is table-driven over the `C` and `F` entries (1530), lazily loaded in a holder class,
  immutable, `IllegalStateException` if the resource is missing. ASCII fast path (A-Z only, verified against the
  table by a test). The unused `lowerCaseChars` was removed.
* `EscapingCaseFoldTest`: written first and committed RED (U+0130, Cherokee, data-driven test failed), then GREEN.
* Behaviour changes versus the approximation: U+0130 now `i` + U+0307, U+0131 now folds to itself (was `i`,
  not predicted), Cherokee letters now fold to capitals.

## Reviewer findings (round 1 of max. 2)

| Finding                                                    | Decision | Reason                                        |
|------------------------------------------------------------|----------|-----------------------------------------------|
| SHA-256 test breaks on CRLF checkouts (no `.gitattributes`) | accepted | added `.gitattributes` with `-text`           |
| Licence text not shipped in the jar                        | accepted | copy placed next to `CaseFolding.txt`         |
| Test parser duplicates the loader's parsing                | rejected | fixed-value tests (ẞ, İ, ligatures, Cherokee) |
|                                                            |          | give independent expectations                 |
| No test of the missing-resource failure path               | rejected | not testable without classloader tricks; the  |
|                                                            |          | path is a trivial null check                  |

The accepted fixes are small and were verified by rebuilding the module (tests green, both files in the jar), so
no second review round was needed.
