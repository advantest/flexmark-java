# CommonMark Update — Execution Plan

Companion to `analysis-comply-with-commonmark.md` (the measured analysis). This file is the **ordered task
list**. Read the analysis for evidence; read this for what happens next.

Base branch: `common-mark-updates`. Target order: **0.29 → 0.30 → 0.31.2**.

Verified 2026-09-30: 0.31.2 (2024-01-28) is still the latest published specification. No new version since
the analysis was written. Measured failure counts are unchanged and remain valid.

---

## ⚠️ Budget reality check — read first

The stated limit is **5% of the plan's total tokens**. My own analysis sizes Task B (reaching 0.31.2) at
**15–30 developer-days**, of which 0.29 alone is the largest share (6 of 9 clusters, including the two
hardest: list indent and code spans).

**Implementing 0.29 is not achievable within 5%.** Attempting it would burn the budget mid-cluster and leave
the tree in a worse state than not starting.

What *is* achievable and genuinely valuable within the budget:

- **Task 0** — machinery fixes + `COMMONMARK_LATEST`. Behaviour-neutral, self-contained, unblocks everything.
- **Task A** — spec files, harness, and full red test suites for 0.29/0.30/0.31.2.

That produces a reviewed, merged foundation plus an executable definition of "done" for every later version.
See Question 1.

---

## Task 0 — Profile machinery fixes + rename (behaviour-neutral)

Branch `feat/commonmark-task0-machinery`. No rendering behaviour changes. Acceptance: the §3 conformance
matrix is **byte-identical** before and after; any moved cell is a bug.

| # | Commit (conventional)                                                              | Content |
| - | ---------------------------------------------------------------------------------- | ------- |
| 1 | `test(parser): assert emulation profile options resolution`                          | Options-resolution test per analysis §5.2. **Must fail** for `COMMONMARK_0_26/27/28/29` (they report `COMMONMARK`). Commit red. |
| 2 | `test(ext-definition): pin definition list indent behaviour`                          | Pins `DefinitionItemBlockParser` lines 116 / 292–295 before disturbing them (§5.3). |
| 3 | `fix(parser): apply list options and profile identity for CommonMark profiles`        | Adds missing `getOptions(dataHolder).setIn(dataHolder)`; removes `setEndOnDoubleBlank(true)` from `COMMONMARK_0_26`; fixes the two profile-vs-family comparisons in `DefinitionItemBlockParser`. **One commit — they are coupled (§5.3).** Turns commit 1 green. |
| 4 | `feat(parser): add COMMONMARK_LATEST version profile`                                 | `public static final ParserEmulationProfile COMMONMARK_LATEST = COMMONMARK_0_28;` (static field, **not** an enum constant — §5.4). `COMMONMARK` stays the family sentinel. Default of `PARSER_EMULATION_PROFILE` points at it. |
| 5 | `test(parser): enforce COMMONMARK_LATEST advancement invariants`                       | The four invariants of §5.4, incl. `spec.txt` ≡ `spec.0.28.txt`. |
| 6 | `docs: record COMMONMARK_LATEST rename in VERSION.md`                                 | States behaviour unchanged at this point. |

## Task A — Spec files, harness, red tests

Branch `feat/commonmark-spec-test-harness`.

| # | Commit                                                                  | Content |
| - | ----------------------------------------------------------------------- | ------- |
| 1 | `test(specs): add CommonMark 0.31.2 specification resources`              | Adds missing `spec.0.31.2.txt` (+ `spec.json` per version if Q3 = JSON). |
| 2 | `test(specs): add spec conformance harness`                               | Productionises the throwaway harness (analysis §8). Per-example pass/fail, version × profile matrix. Pitfalls: `→`(U+2192)→tab, `PERCENT_ENCODE_URLS=true`. |
| 3 | `test(core): add full spec tests for 0.26, 0.30, 0.31.2`                   | Missing `FullOrigSpec026/030/0312CoreTest`; register in `CoreRendererTestSuite`. |
| 4 | `test(core): enable 0.29 spec test with known-failures baseline`           | Removes the `ResourceLocation.NULL` stub. Baseline per Q4. |

Expected end state (measured, from the analysis): **0.29 → 20 failures, 0.30 → 23, 0.31.2 → 26**, all
recorded as a shrink-only baseline. Exact per-version cluster mapping is produced *by the harness*, not
assumed.

## Task B29 / B30 / B312 — per-version implementation (NOT in this budget)

One branch per version, stacked, each independently reviewable and mergeable:
`feat/commonmark-0.29` → `feat/commonmark-0.30` → `feat/commonmark-0.31.2` (see Q2).

Per version, repeated: TDD per cluster (red → green), one commit per cluster, advance `COMMONMARK_LATEST`
only when that version's `FullOrigSpec0xxCoreTest` is at **zero** failures, `VERSION.md` entry, then a
review sub-agent pass before the next version starts.

Cluster → version ownership (analysis §6): **0.29** owns code spans, link destinations `<>`, emphasis ÷3,
list indent ≥4, info strings, entity/case-fold. **0.30** owns `<textarea>`. **0.31.2** owns HTML comments,
Unicode punctuation.

Order within 0.29 — cheapest and least coupled first: info strings → entity/case-fold → code spans → link
destinations → emphasis ÷3 → **list indent last** (hardest; ~30 interacting list flags, affects every
emulation family).

---

## Questions — please decide before I implement

1. **Scope for this budget.** Confirm: Task 0 + Task A only, stopping before any 0.29 behaviour change?
   (My recommendation. Alternative: Task 0 only, done very thoroughly.)
2. **Branching.** Stacked branches per version as above, or one branch with a git tag per version? Stacked
   branches review better; tags are simpler. Which?
3. **Test source.** Drive tests from the existing `spec.txt` `FullOrigSpec*` pattern, or from `spec.json`
   (gives upstream example number + section in failure messages, but adds a JSON dependency — Jackson or
   Gson — to the test module)? Is a new test-scope dependency acceptable?
4. **Red tests and CI.** You asked for failing tests first, but `main`/CI should presumably stay green. How
   should the 26 known failures land — (a) shrink-only known-failures baseline file, (b) `@Ignore`d per
   example, or (c) accepted-actual-output baseline? I recommend (a).
5. **Flag granularity.** One `DataKey` per breaking change (~11 flags, per the analysis), so every profile
   is exactly reconstructible — or one coarse "spec version" switch read at parse time? I recommend the
   former; it is more work but is what makes `COMMONMARK_0_28` survive the 0.29 default change.
6. **Definition-list behaviour change.** The §5.3 fix in Task 0 commit 3 *will* alter `ext-definition`
   parsing for versioned profiles (it works by accident today). If its committed spec resource files need
   regenerating, do you want to review that diff explicitly before I commit it?
7. **`endOnDoubleBlank` removal.** Confirm removing `setEndOnDoubleBlank(true)` from `COMMONMARK_0_26` — no
   current test covers the rule, and decision 1 says 0.26 removed it.
8. **Review agent.** Confirm a `code-review` sub-agent pass after each version (and after Task 0), with
   findings reported to you rather than auto-applied.

---

## Constraints I am operating under

- **Never push.** Commits only, on my branches.
- JDK 21+ required (`maven-enforcer`); `JAVA_HOME` must be set per invocation — the machine default is 17.
- Never silently regenerate extension spec resource files (analysis §7.4 guardrail 3).
- **I cannot query your Copilot plan usage** — no tool exposes it. I will track my own consumption and stop
  at the agreed scope boundary; please tell me if you see the limit approaching sooner.
