# Review B29 default profile: CommonMark 0.29 as the parsing default

Scope: commits `2d46bcfc1^..HEAD` on `common-mark-updates`. The nine 0.29 options now default to the 0.29 behaviour
and every other profile and family opts out. Reviewed by a `code-review` sub-agent (one round).

## Findings

| # | Finding                                                                                      | Outcome  |
|---|----------------------------------------------------------------------------------------------|----------|
| 1 | Opt-outs complete for every enum profile and family in `setIn` and `getOptions`              | Confirmed |
| 2 | `CoreNodeFormatter` read `isNoItemAtCodeIndent()` without a family guard                     | Applied  |
| 3 | The eight parser keys leak 0.29 when only `PARSER_EMULATION_PROFILE` is set, no profile applied | Declined |
| 4 | VERSION.md lacked the visible effects and the "profile must be applied" caveat               | Applied  |
| 5 | `StrikethroughTest.threeInnerThree` pinned 0.28 and lost the default (0.29) assertion        | Applied  |

## Applied

* Formatter: the list-item-like continuation line is kept indented only when the list options' emulation
  profile is in the `COMMONMARK` family, mirroring `ListItemParser`.
* VERSION.md: states that opting out happens when a profile is applied, and names the visible effects.
* `StrikethroughTest`: added a test asserting the 0.29 default output for `~~~foo~~~` next to the pinned 0.28 one.

## Declined

* Resolving the keys from `PARSER_EMULATION_PROFILE` inside the parser components (finding 3). Setting the
  profile key alone never applied a profile's options in this code base; that is the pre-existing model and the
  premise of this task. Changing it would be a separate design change. It is documented in VERSION.md instead.

## Round 2: full-spec coverage

Reviewed FullSpec029DefaultOptionsCoreTest, FullOrigSpec026CoreTest and the docs. No leakage of profile options in the
default test, 0.26 does not pass vacuously (it fails under the 0.29 profile). One finding, line-ending churn in the
plan document, was applied. Result: 0.26 passes all 618 examples, no known-failures baseline was needed.
