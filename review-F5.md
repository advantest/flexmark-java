# Review F5: directional punctuation patterns

## What changed

* `Parsing.punctuationOpen` / `punctuationClose`: the `&&` now lives inside a character class and every alternative is
  anchored. `Ps` is only in the opening set, `Pe` only in the closing set, `Pi`, `Pf` and the rest in both. The old/new
  variant pairs (without/with the `S` categories) are kept and still chosen by `UNICODE_PUNCTUATION_INCLUDES_SYMBOLS`.
* `PUNCTUATION_ONLY` and its variants were deleted; a repo-wide search found no code reference.
* `DirectionalPunctuationTest` (13 tests): code point sweep of both sets for both variants, bracket asymmetry,
  guillemets, em dash, ideographic full stop, symbols (with and without the symbols option), the five damage inputs,
  the six CJK examples, option-off control, formatter round trip. Examples 10 to 12 were added to
  `core_extra_ast_spec.md`.
* TDD: the red commit failed 10 of 13 tests on the old code, the green commit passes all.
* The formatter needed no change: the round trip with the option on renders identically.
* Full `mvn -o install`: BUILD SUCCESS, 3:34 min. The CommonMark spec suites are unchanged (option is not spec).

## Review (code-review sub-agent, 1 round)

| Finding                                                                 | Decision            |
|-------------------------------------------------------------------------|---------------------|
| No significant issues; regex valid, ASCII `<` `>` `\` classification kept | Confirmed, no change |
| Sweep derives expectations from `Character.getType`, not tautological   | Confirmed, no change |

## Notes

* Not verified: behaviour on JDKs other than 21 (the sweep uses the running JDK's Unicode data on both sides).
