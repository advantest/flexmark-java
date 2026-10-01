# Review F6: pin extension fallout of symbols-as-punctuation

## What changed

* `SymbolPunctuationExtensionFalloutTest` (module `flexmark-ext-gfm-strikethrough`): nine inputs under the 0.30
  profile, plain defaults, the explicit `COMMONMARK_0_31_2` profile and 0.31.2 with
  `UNICODE_PUNCTUATION_INCLUDES_SYMBOLS=false`; an opt-out equivalence property (opt-out equals 0.30 for every
  input); `COMMONMARK_0_28` and `COMMONMARK_0_29` for `a~~©x~~b` and `H~°~O`.
* `VERSION.md`: extension fallout, before/after table, opt-out recipe, deliberate GitHub/cmark-gfm divergence.
* `README.md`: short paragraph and code sample in "Markdown Processor Emulation".
* `comply-with-commonmark-plan.md`: Task F6 marked DONE, pointing at the test.
* No production code was changed. All measured values matched the table in the task; none had to be adjusted.
* Meaningfulness check: flipping the expected value of `a~~©x~~b` for the 0.30 profile made 2 tests fail; it was
  restored and the test passes again.
* Full `mvn -o install`: BUILD SUCCESS.

## Review (code-review sub-agent, 1 round)

| Finding                                                          | Decision                                      |
|------------------------------------------------------------------|-----------------------------------------------|
| Plan referenced `review-F6.md`, which did not exist yet          | Accepted: this file was added                 |
| Opt-out order (profile before key) truly exercises the opt-out   | Confirmed, no change                          |
| No Markdown line over 120 chars, VERSION.md table aligned        | Confirmed, no change                          |

## Notes

* The opt-out is applied through `profile.setIn(options)` followed by an explicit key assignment, so the key wins.
* The plan's header summary (line 52) still lists F6 as an open follow-up; it was left untouched on purpose.
