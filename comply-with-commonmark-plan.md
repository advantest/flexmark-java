# CommonMark Update — Execution Plan

Companion to `analysis-comply-with-commonmark.md` (the measured analysis). This file is the **ordered task
list**. Read the analysis for evidence; read this for what happens next.

Base branch: `common-mark-updates`. Target order: **0.29 → 0.30 → 0.31.2**.

Verified 2026-09-30: 0.31.2 (2024-01-28) is still the latest published specification. No new version since
the analysis was written. Measured failure counts are unchanged and remain valid.

---

## Decisions taken

| # | Decision |
| - | -------- |
| 1 | Proceed **one CommonMark version at a time**. Task 0 first, then Task A and B for 0.29 only. Versions above 0.29 are planned after 0.29 is green. Completing 0.26 example coverage is a separate task. |
| 2 | **One branch** (`common-mark-updates`) with a **git tag per milestone**, rather than stacked branches. |
| 3 | Keep the existing `spec.txt` / `FullOrigSpec*` test convention. `spec.json` may be adopted later only if failure diagnostics prove painful. |
| 4 | Known-failures **baseline** for not yet implemented examples, so CI stays green and the count can only shrink. |
| 5 | **One flag per breaking change**, in the style of the existing `Parser` `DataKey` options, so every profile stays exactly reconstructible. |
| 6 | Commit first, review later. Treat the branch as one or more pull requests. |
| 7 | Confirm each decision by test before implementing it. If a decision turns out to be wrong, stop and ask. |
| 8 | Review sub-agent after Task 0 and after every larger task or new version. Apply the improvements, record review and declined suggestions in Markdown. **Max 2 review rounds per task.** |

Decision 7 already paid off: decision 1 of the analysis (`COMMONMARK_0_26` `endOnDoubleBlank` is a bug)
was **confirmed** against the 0.25 → 0.26 specification diff before the fix was written.

## Status

- **Task 0 — done and reviewed.** Tag `commonmark-profile-machinery-fixed`. Full test suite green.
- **Task A — next**, scoped to CommonMark 0.29 only.

---

## Task 0 — Profile machinery fixes + rename (behaviour-neutral)

## Task 0 — Profile machinery fixes + `COMMONMARK_LATEST` (DONE, behaviour-neutral)

Committed on `common-mark-updates`, tag `commonmark-profile-machinery-fixed`. No rendering behaviour
changes: the full test suite passed unchanged after every step.

| # | Commit (conventional)                                                              | Result |
| - | ---------------------------------------------------------------------------------- | ------ |
| 1 | `test(parser): assert emulation profile options resolution`                          | Committed red: 3 of 4 tests failed, proving both defects of analysis §5.2. |
| 2 | `fix(parser): apply list options and profile identity for CommonMark profiles`        | Adds the missing `getOptions(dataHolder).setIn(dataHolder)` and profile tagging; drops `setEndOnDoubleBlank(true)` from `COMMONMARK_0_26`; fixes the two profile-vs-family comparisons in `DefinitionItemBlockParser`. **One commit — they are coupled (§5.3).** Turned commit 1 green. |
| 3 | `feat(parser): add COMMONMARK_LATEST version profile`                                 | `final public static ParserEmulationProfile COMMONMARK_LATEST = COMMONMARK_0_28;` (static field, **not** an enum constant — §5.4). `COMMONMARK` stays the family sentinel. Default of `PARSER_EMULATION_PROFILE` points at it. Includes the advancement invariants, incl. `spec.txt` ≡ `spec.0.28.txt`. |
| 4 | `docs: record COMMONMARK_LATEST and profile fixes in VERSION.md`                       | Including the breaking-change note on the changed default. |
| 5 | `test(parser): strengthen emulation profile option resolution coverage`                | Review round 1 improvements; review in `review-task0-profile-machinery.md`. |

The separately planned commit "pin definition list indent behaviour" was dropped: the existing
`flexmark-ext-definition` spec tests plus the full suite already pin that behaviour, so a new
characterization test would have added no information.

Confirmed by evidence, not assumed:

- 0.26 **removed** the "two blank lines end a list" rule, so enabling it for `COMMONMARK_0_26` was
  inverted. Checked against the 0.25 → 0.26 diff at <https://spec.commonmark.org/0.26/changes.html>.
- In `DefinitionItemBlockParser` the `FIXED_INDENT` half of the indent check stays a **profile**
  comparison while the CommonMark half uses the **family**. Using the family for both would have changed
  `MULTI_MARKDOWN` and `PEGDOWN*` behaviour, which Task 0 must not do.

## Task A — Spec files, harness, red tests for CommonMark 0.29 only

Branch `feat/commonmark-spec-test-harness`. **Scope: CommonMark 0.29 only.** Completing the example
coverage for 0.26 is a separate task. Tasks for versions above 0.29 are added later, one version at a
time.

| # | Commit                                                                  | Content |
| - | ----------------------------------------------------------------------- | ------- |
| 1 | `test(specs): add spec conformance harness`                               | Productionises the throwaway harness (analysis §8). Per-example pass/fail against a spec resource. Pitfalls: `→`(U+2192)→tab, `PERCENT_ENCODE_URLS=true`. |
| 2 | `test(core): enable 0.29 spec test with known-failures baseline`          | Removes the `ResourceLocation.NULL` stub from `FullOrigSpec029CoreTest`. `spec.0.29.txt` already exists in `flexmark-test-specs`; no new spec resource needed. |

Expected end state (measured, from the analysis): **0.29 → 20 failures**, recorded as a shrink-only
baseline so CI stays green while the count can only decrease. The exact failing example numbers are
produced *by the harness*, not assumed.

Deferred, not part of Task A:

- 0.26 example coverage (`FullOrigSpec026CoreTest`) — separate task.
- `spec.0.31.2.txt` and the 0.30 / 0.31.2 test classes — added with their own version tasks.

## Task B29 — CommonMark 0.29 implementation (next, after Task A)

Branch `feat/commonmark-0.29`, tagged on completion. Versions above 0.29 are planned only once 0.29 is
green.

Per cluster: TDD red → green, one commit per cluster. Advance `COMMONMARK_LATEST` to `COMMONMARK_0_29`
only when `FullOrigSpec029CoreTest` reports **zero** failures, add the `VERSION.md` entry, then a review
sub-agent pass (max 2 rounds).

0.29 owns 6 of the 9 known clusters (analysis §6). Order — cheapest and least coupled first:
info strings → entity/case-fold → code spans → link destinations `<>` → emphasis ÷3 →
**list items indented 4+ last** (hardest; ~30 interacting list flags, affects every emulation family).

Clusters owned by later versions, for context only: **0.30** `<textarea>`; **0.31.2** HTML comments and
the Unicode punctuation set.

---

## Open questions

None blocking Task A. Raise a question only if a recorded decision turns out to be contradicted by the
code or the specification (decision 7).
## Constraints I am operating under

- **Never push.** Commits only, on my branches.
- JDK 21+ required (`maven-enforcer`); `JAVA_HOME` must be set per invocation — the machine default is 17.
- Never silently regenerate extension spec resource files (analysis §7.4 guardrail 3).
- **I cannot query your Copilot plan usage** — no tool exposes it. I will track my own consumption and stop
  at the agreed scope boundary; please tell me if you see the limit approaching sooner.
