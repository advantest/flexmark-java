# Review B29 finalization

Scope: enabling `FullOrigSpec029CoreTest`, retiring the known-failures baseline, advancing `COMMONMARK_LATEST` to
`COMMONMARK_0_29`, documentation. One review round (sub-agent, Claude Sonnet 5.5).

## Findings and decisions

| # | Finding                                                                              | Decision |
| - | ------------------------------------------------------------------------------------ | -------- |
| 1 | `VERSION.md` did not say that applying `COMMONMARK_LATEST` as a profile now enables 0.29 parsing. | Applied. |
| 2 | `comply-with-commonmark-plan.md` referenced this file before it existed.             | Applied, this file. |
| 3 | The plan still described the baseline and the disabled full test as current.         | Applied, marked as retired/historical. |
| 4 | Stale comment "implement 0.29 as defaults with 0.28 as changes" in `ParserEmulationProfile`. | Declined: a comment from the earlier machinery work, unrelated to this change; the code is untouched. |

No finding: 0.26/0.27/0.28 defaults and branches untouched (only `COMMONMARK_LATEST` and its Javadoc changed in
main code); no dead code left by the FAIL removal (`SpecExample.withOptionsSet` and `TestUtils.FAIL` are public
test-util API used elsewhere); the full-spec test covers the pristine `spec.0.29.txt` with the 0.29 profile and is
registered in `CoreRendererTestSuite`.

## Decisions taken during the work

- `spec.txt` became a copy of `spec.0.29.txt`. `ParserEmulationProfileTest` and the analysis (invariant 3)
  require it to track `COMMONMARK_LATEST`. The pristine `spec.0.xx.txt` files are untouched.
- `FullOrigSpecCoreTest` and `SpecIntegrationTest` read `spec.txt`, so they now apply the `COMMONMARK_LATEST`
  profile options; without them the 0.29 examples fail against the 0.28 option defaults.
- The six cluster tests listed `COMMONMARK_LATEST` among the pre-0.29 profiles, which stopped being true. It was
  removed there; `COMMONMARK_0_28` still covers the case.
- The neutrality test `defaultListOptionsEqualTheCommonMarkLatestProfileOptions` was reformulated: default list
  options equal the latest profile's except `LISTS_NO_ITEM_AT_CODE_INDENT`, which the 0.29 profile enables by design.
- The global example counter of `ComboOrigSpec029CoreTest` was dropped: it only fed the baseline lookup.
- Label/behaviour split: the default `PARSER_EMULATION_PROFILE` is now `COMMONMARK_0_29` while option defaults still
  behave as 0.28. Nothing in the code compares against `COMMONMARK_0_28`; recorded in `VERSION.md`.