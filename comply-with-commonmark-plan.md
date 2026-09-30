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
| 4 | Known-failures **baseline** for not yet implemented examples, so CI stays green and the count can only shrink. Retired after B29: the baseline reached zero and was deleted. |
| 5 | **One flag per breaking change**, in the style of the existing `Parser` `DataKey` options, so every profile stays exactly reconstructible. |
| 6 | Commit first, review later. Treat the branch as one or more pull requests. |
| 7 | Confirm each decision by test before implementing it. If a decision turns out to be wrong, stop and ask. |
| 8 | Review sub-agent after Task 0 and after every larger task or new version. Apply the improvements, record review and declined suggestions in Markdown. **Max 2 review rounds per task.** |
| 9 | **The parser preserves the raw source text.** flexmark must keep exact source tracking, because FluentMark and other tools depend on it. Text normalization therefore happens at **render** time, never by rewriting node text while parsing. A change that alters AST *structure* — which delimiters pair up, how blocks nest — still belongs in the parser, because it does not rewrite source text. |

Decision 7 already paid off: decision 1 of the analysis (`COMMONMARK_0_26` `endOnDoubleBlank` is a bug)
was **confirmed** against the 0.25 → 0.26 specification diff before the fix was written.

## Status

- **Task 0 — done and reviewed.** Tag `commonmark-profile-machinery-fixed`. Full test suite green.
- **Task A — done and reviewed.** Tag `commonmark-0.29-spec-tests`. Full test suite green.
- **Task B29 — done.** All six clusters done, tag `commonmark-0.29.0-compliant` (the intermediate cluster tags were
  removed once the milestone was reached). `COMMONMARK_LATEST` is now `COMMONMARK_0_29`, 0.29 is the **default**
  parsing behaviour, and the known-failures baseline is retired. Reviews: `review-B29-finalization.md`,
  `review-B29-default-profile.md`.
- **Task A30 — done (measurement only).** `spec.0.30.txt` added, the gap was 3 of 652 examples.
- **Task B30 — done.** All clusters done (B30.1 was a 0.29 leftover, fixed earlier), plus two latent gaps no spec
  example covers: HTML declarations (#620) and VT/FF in tags (#618). `COMMONMARK_LATEST` is now
  `COMMONMARK_0_30`, 0.30 is the **default**, `spec.txt` tracks 0.30 and the known-failures baseline is retired.
  Review: `review-B30.md`.
- **Task A312 — done (measurement only).** `spec.0.31.2.txt` added (652 examples, 2024-01-28), the gap under the
  default configuration is **3 of 652** examples. The substantial work is in the **latent** gaps, not the failing
  examples. Next: Task B312.

---

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

## Task A — Per-example tests for CommonMark 0.29 only (DONE, tests only)

**Scope: CommonMark 0.29 only.** Completing the example coverage for 0.26 is a separate task. Tasks for
versions above 0.29 are added later, one version at a time.

| # | Commit                                                                        | Content |
| - | ------------------------------------------------------------------------------ | ------- |
| 1 | `test(core): cover CommonMark 0.29 spec per example with known-failures baseline` | `ComboOrigSpec029CoreTest`: one test per example of the unmodified `spec.0.29.txt` (649 examples). |
| 2 | `test(core): harden the CommonMark 0.29 known-failures baseline`                 | Review round 1 improvements; review in `review-taskA-spec029-tests.md`. |

The separately planned "spec conformance harness" commit was dropped. flexmark already has the needed
mechanism: the per-example `FAIL` option makes `RenderingTestCase` expect a `ComparisonFailure`, so a
listed example passes while it renders incorrectly and **fails as soon as it renders correctly**. That is
exactly the shrink-only ratchet of decision 4, so no new machinery was written.

Design points:

- The upstream `spec.0.29.txt` stays pristine. `FAIL` is injected into the test data, not into the file.
- (Retired after B29, historical.) The baseline lived in `spec.0.29.known-failures.txt` of `flexmark-core-test`, keyed by
  the spec's global example number, with the rule that entries may only be removed.
- `FullOrigSpec029CoreTest` was deliberately **disabled**. It was the final zero-failures gate, enabled
  when the baseline is empty.

Measured end state: **20 of 649 examples fail**, matching the analysis. Proven, not assumed: empty
baseline → 20 failures; full baseline → 0 failures; removing a single entry → exactly that one failure.
Because a wrongly aimed entry would itself trip the ratchet, the run with 0 failures proves all 20
entries land exactly on the 20 failing examples.

The 20 failures by section: code spans (7), link reference definitions (4), links (3), fenced code
blocks (2), lists (2), emphasis (2). The cluster comments in the baseline file are a provisional reading
of the example sources, **not** verified root causes — confirm the cause before fixing.

Deferred, not part of Task A:

- 0.26 example coverage (`FullOrigSpec026CoreTest`) — separate task.
- `spec.0.31.2.txt` and the 0.30 / 0.31.2 test classes — added with their own version tasks.

## Task B29 — CommonMark 0.29 implementation, split into six clusters (DONE)

Each cluster is one 0.29 change, taken from the CommonMark changelog, section `[0.29]`
(<https://github.com/commonmark/commonmark-spec/blob/master/changelog.txt>). The clusters are
independent except where noted, and each ends with an empty section of
`spec.0.29.known-failures.txt`, a green full suite, a review and a commit.

Ordered smallest and most isolated first, so the delegation loop is validated on a cheap cluster:

| #     | Cluster                            | Examples                  | 0.29 change | Risk | Tag |
| ----- | ---------------------------------- | ------------------------- | ----------- | ---- | --- |
| B29.1 | Info strings of fenced code blocks | 116                       | Backticks disallowed in info strings after **backtick** fences only; both backticks and tildes allowed after **tilde** fences (#119). Info string is trimmed of all whitespace, not only spaces (#505). | low | DONE `commonmark-0.29-info-strings` |
| B29.2 | Code spans                         | 108, 331–337, 637         | Line endings become spaces; strip one space from each end only if the content is **not** entirely spaces (#569); never collapse interior space (#532). | medium | DONE `commonmark-0.29-code-spans` |
| B29.3 | Emphasis, rule of three            | 415, 416                  | Interior delimiter runs match if **both** run lengths are multiples of 3 (#528). | medium | DONE `commonmark-0.29-emphasis-rule-of-3` |
| B29.4 | Link destinations in `<...>`       | 486, 490, 491             | Spaces allowed again inside `<...>`, reverting 0.24 (#503); a destination may not begin with `<` unless inside `<...>` (#538). | medium | DONE `commonmark-0.29-link-destinations` |
| B29.5 | Link reference definitions         | 164, 170, 184, 185        | Setext heading after definitions (#395); unused definition (#454); space required before the title (#469). | medium | DONE `commonmark-0.29-link-reference-definitions` |
| B29.6 | List items indented 4+ spaces      | 282, 283                  | Such lines are continuation lines when not blank, indented code otherwise (#497); drops the vestigial "not separated by more than one blank line" restriction (#543). | **high** | DONE `commonmark-0.29-list-item-indent` |

Clusters 4 and 5 both touch link destination parsing, so 4 runs before 5.

Correction found while confirming the causes: example 108 appears in the *Fenced code blocks* section of
the spec but is a **code span** case, so it belongs to cluster 2, not cluster 1. The provisional cluster
labels written during task A were wrong here; the baseline file now carries the confirmed causes with
their spec issue numbers.

### Division of labour

- **Orchestrator**: spec interpretation and root-cause confirmation, cluster definition and ordering,
  decision-7 calls (stop and ask when a recorded decision turns out to be wrong), plan and `VERSION.md`,
  tagging, and all questions to the user.
- **Implementation sub-agent** (Claude Sonnet 5.5), one per cluster: red tests first, implement, run the
  full suite, drive its own review sub-agent for at most 2 rounds, apply the improvements, write the
  review Markdown, and commit each concern separately.

### Old, superseded notes

Branch `feat/commonmark-0.29`, tagged on completion. Versions above 0.29 are planned only once 0.29 is
green.

Per cluster: TDD red → green, one commit per cluster. Advance `COMMONMARK_LATEST` to `COMMONMARK_0_29`
only when `FullOrigSpec029CoreTest` reports **zero** failures, add the `VERSION.md` entry, then a review
sub-agent pass (max 2 rounds).

Follow-up (done): the option defaults follow the newest supported spec (0.29 is the `DataKey` default) and older
profiles opt out, so a plain `Parser.builder()` parses as 0.29. Rule for the next version: flip the new options'
defaults and make `COMMONMARK_0_29` and older profiles set them back; see `review-B29-default-profile.md`.

0.29 owns 6 of the 9 known clusters (analysis §6). Order — cheapest and least coupled first:
info strings → entity/case-fold → code spans → link destinations `<>` → emphasis ÷3 →
**list items indented 4+ last** (hardest; ~30 interacting list flags, affects every emulation family).

Clusters owned by later versions, for context only: **0.30** `<textarea>`; **0.31.2** HTML comments and
the Unicode punctuation set.

---

## Task A30 — Measure the CommonMark 0.30 gap (DONE, tests only)

| # | Commit                                                    | Content |
| - | --------------------------------------------------------- | ------- |
| 1 | `test(specs): add the pristine CommonMark 0.30 specification` | `spec.0.30.txt` from `commonmark-spec` tag `0.30`, byte-exact, `version: 0.30`, 652 examples. |
| 2 | `test(core): measure the CommonMark 0.30 gap`                | `ComboOrigSpec030CoreTest` (one test per example, **default configuration, no profile applied**) plus `spec.0.30.known-failures.txt`. |

Measured against the current default (0.29) behaviour: **3 of 652 examples fail** — 28, 171, 539.
Proven the same way as for 0.29: empty baseline → 3 failures, populated baseline → 0 failures.

0.30 is overwhelmingly an **editorial** release — moved sections, reworded character-group definitions,
typos, tooling. Only three changelog items change rendering, and three more are latent (no spec example
forces them, see below).

## Task B30 — CommonMark 0.30 implementation (DONE)

Done. Outcome: new options `Parser.HTML_BLOCK_TEXTAREA_TYPE_1` (B30.2), `REFERENCE_LABEL_UNICODE_CASE_FOLD` (B30.3),
`HTML_DECLARATION_ASCII_LETTER` (#620) and `HTML_TAG_WHITESPACE_NO_VT_FF` (#618), all default `true` and set to
`false` by every profile except `COMMONMARK_0_30`. The case fold is a Java approximation, not full Unicode
`CaseFolding.txt`.

### Latent gaps (no 0.30 example covers them, hand-written tests instead)

| Gap                             | Status | Tests                     |
|---------------------------------|--------|---------------------------|
| HTML declarations (#620)        | done   | `HtmlDeclarationTest`     |
| VT/FF not tag whitespace (#618) | done   | `HtmlTagWhitespaceTest`   |

Three clusters, each independent. All are **small**; 0.30 is far cheaper than 0.29 was.

| #     | Cluster        | Examples | 0.30 change | Classes | Risk |
| ----- | -------------- | -------- | ----------- | ------- | ---- |
| B30.1 | Entity length  | 28       | Numeric character references are limited to 7 decimal / 6 hex digits; longer ones stay literal (#575). | `Parsing` (`ST_ENTITY_*`) | low |
| B30.2 | `textarea` HTML block | 171 | `textarea` joins `script`, `style`, `pre` as a **type 1** literal HTML block, so it may contain blank lines (#657, #667). | `HtmlBlockParser`, `Parsing` | low |
| B30.3 | Unicode case fold of link labels | 539 | Label matching needs real Unicode case folding: `[ẞ]` must match `[SS]`. Lower-casing alone is not enough (#582). | `Escaping.normalizeReference` | low |

Confirmed root causes, not assumed — each was checked against the normative 0.30 text and the flexmark code:

- **B30.1** is really a **leftover 0.29 item** (#487 limited the lengths in 0.29). `Parsing` still uses
  `#[0-9]{1,8}` and `#x[a-f0-9]{1,8}`, so 0.29 never fully complied; its own example passed by luck
  because it used 9 digits. The hex case is wrong too but **no spec example covers it**.
- **B30.2**: flexmark currently treats `<textarea>` as a type 7 block, so it ends at the first blank line.
- **B30.3**: `normalizeReference` only calls `toLowerCase()`; U+1E9E lower-cases to `ß`, not `ss`.
  Probes show `[ß]`/`[SS]` is also wrong. Fold via `toUpperCase().toLowerCase()`, and normalise **only the
  lookup key**, never the node text — decision 9.

### Latent gaps — no failing example, so the baseline cannot enforce them

Read from the normative text and confirmed by probes. **Needs a decision (see Open questions).**

| Gap | Evidence | Classes |
| --- | -------- | ------- |
| HTML declarations need not be all-capital ASCII (#620) | `ST_DECLARATION_*` is `<![A-Z]+\s+[^>]*>`; `<!a>`, `<!DOCTYPE>` are neither inline raw HTML nor HTML blocks | `Parsing`, `HtmlBlockParser` |
| Hex entity length (part of #575) | `&#x1234567;` renders U+FFFD, must stay literal | `Parsing` |
| VT/FF no longer whitespace after a tag name (#618) | flexmark uses `\s`, so `<pre\v>` and `<div\f>` still start HTML blocks | `Parsing` |

### Method

Same as B29 and unchanged by 0.30's small size: TDD red first, one commit per concern, full suite green,
review sub-agent (max 2 rounds) with a `review-B30.*.md`, and the known-failures baseline shrinking to
zero. Per the rule established at the end of B29, the **new behaviour becomes the `DataKey` default** and
`COMMONMARK_0_29` and older profiles opt out of it in `ParserEmulationProfile`.

Created as part of this task (they did not exist before):


- the `COMMONMARK_0_30` enum constant and its `setIn` / `getOptions` wiring,
- `FullOrigSpec030CoreTest` (profile applied) and a default-options variant, registered in
  `CoreRendererTestSuite`,
- advancing `COMMONMARK_LATEST` to `COMMONMARK_0_30` and `spec.txt` to the 0.30 spec, **only** once the
  baseline is empty, with the `VERSION.md` breaking-change note.

Cluster order: B30.1 → B30.2 → B30.3. They do not interact; B30.3 is listed last because case folding is
the only one with a plausible effect on extensions that resolve references (footnotes, abbreviations).
B30.2 should check the **formatter** round-trip: three of the six 0.29 clusters needed an extra formatter
fix because the formatter re-emitted syntax that no longer parsed.

---

## Open questions

Both questions that preceded Task B30 have been decided and implemented:

1. **The three latent gaps are implemented**, each with hand-written tests, because no spec example forces
   them. This established the standing rule: where the spec's own examples do not cover a spec change, we
   write our own tests — the goal is full compliance, not merely passing the supplied examples.
2. **The entity length fix was a 0.29 leftover** and was treated as a plain bug fix rather than a gated
   `COMMONMARK_0_30` option, so it also corrects `COMMONMARK_0_29` and the default configuration. Profiles
   0.26 to 0.28 keep the old 8-digit limit, which their own spec versions mandate.

Otherwise raise a question only if a recorded decision turns out to be contradicted by the code or the
specification (decision 7).

## Task A312 — Measure the CommonMark 0.31.2 gap (DONE, tests only)

`spec.0.31.2.txt` is vendored pristine (652 examples, version `0.31.2`, date 2024-01-28, LF line endings).
`ComboOrigSpec0312CoreTest` runs the **default** configuration against it with the usual shrink-only
`spec.0.31.2.known-failures.txt` baseline and the `FAIL` ratchet.

0.31.2 and 0.31.1 are packaging-only releases; all normative change comes from **0.31**. The changelog has
exactly four normative items, and only two of them are covered by an example:

| Changelog item                                     | Normative? | Example        |
|----------------------------------------------------|------------|----------------|
| Add symbols to Unicode punctuation                 | yes        | 354            |
| Remove the restrictive limitation on inline comments | yes      | 625, 626       |
| Remove `source` from the HTML block type 6 tag list | yes       | none — latent  |
| Add `search` to the HTML block type 6 tag list      | yes       | none — latent  |

Everything else in [0.31] is editorial: link and typo fixes, "compact" → "collapsed", the removal of "first"
before "link label", and tooling entries. The Unicode whitespace definition was reworded but not changed,
and flexmark's `UNICODE_WHITESPACE_CHAR` already matches it exactly.

## Task B312 — CommonMark 0.31.2 implementation (planned)

Only three examples fail, but **this is not a small task**: the punctuation cluster has by far the widest
blast radius of any change in this whole effort, and the genuinely important defects are latent.

| #      | Cluster                | Examples | Change | Classes | Risk |
|--------|------------------------|----------|--------|---------|------|
| B312.1 | HTML comment grammar   | 625, 626 | Comment text may contain `--`; `<!-->` and `<!--->` are complete empty comments. | `Parsing.ST_HTMLCOMMENT` | low |
| B312.2 | Block tag list         | none     | `search` joins the type 6 tag list, `source` leaves it. | `Parser.HTML_BLOCK_TAGS`, `HtmlDeepParser.BLOCK_TAGS` | low |
| B312.3 | Symbols are Unicode punctuation | 354 | Unicode `S*` (Sm, Sc, Sk, So) counts as punctuation for the emphasis flanking rules. | `Parsing.ST_PUNCTUATION*`, `InlineParserImpl.scanDelimiters` | **medium-high** |

Order: B312.1 → B312.2 → B312.3. The first two are independent and cheap; B312.3 is last because it is the
only one that can disturb every extension which processes delimiters.

### Confirmed root causes

- **B312.1** — `Parsing.ST_HTMLCOMMENT` is `<!---->|<!--(?:-?[^>-])(?:-?[^-])*-->`, which forbids `--` inside
  the text. Block-level comments (HTML block type 2) are **already correct**; only the inline rule is wrong.
  `HTML_TAG` is built from this at `Parsing.java:482` and is **cached**, so the cache key must include the
  new flag.
- **B312.2** — `Parser.HTML_BLOCK_TAGS` (`Parser.java:208+`) still lists `source` and lacks `search`. Probed:
  `<search>*foo*` is wrongly a paragraph, `<source>*foo*` is wrongly a raw HTML block. Note the list also
  contains `math`, which **no** CommonMark version lists — a pre-existing deviation, out of scope here but
  worth recording. `HtmlDeepParser.BLOCK_TAGS` is a second, independent hard-coded list with the same defect;
  it only applies when `HTML_BLOCK_DEEP_PARSER` is enabled, which is off by default, but it must be fixed too.
- **B312.3** — two independent defects, both needed:
  1. `ST_PUNCTUATION`, `ST_PUNCTUATION_OPEN`, `ST_PUNCTUATION_CLOSE` and `ST_PUNCTUATION_ONLY` cover only
     `\p{Pc}\p{Pd}\p{Pe}\p{Pf}\p{Pi}\p{Po}\p{Ps}` plus ASCII — the whole `S` group is missing.
  2. `InlineParserImpl.scanDelimiters` derives the characters before and after a delimiter run from a single
     UTF-16 `char`, so **supplementary code points are never classified correctly**. This is a latent
     **0.30-and-earlier** bug as well: 217 supplementary `P*` code points are already misclassified today.

Measured on JDK 21 (Unicode 15.0), flexmark misses roughly **7,978** code points that 0.31.2 treats as
punctuation — 930 Sm, 56 Sc, 118 Sk and 2,731 So in the BMP, plus 3,926 supplementary. The spec's own
examples detect only the BMP currency symbols, via example 354. Emoji (`a*😀*b`) and all mathematical and
modifier symbols are undetected by the spec suite yet clearly in scope.

### Latent gaps — no failing example, so the baseline cannot enforce them

Each needs hand-written tests, per the standing rule.

| Gap                                            | Evidence                                                                    | Belongs to |
|------------------------------------------------|-----------------------------------------------------------------------------|------------|
| `search` missing from the type 6 tag list       | `<search>*foo*` is a paragraph; must start a block and interrupt a paragraph | 0.31       |
| `source` still in the type 6 tag list           | `<source>*foo*` becomes a raw HTML block; must not                           | 0.31       |
| Supplementary code points misclassified         | `scanDelimiters` reads one `char`; 217 supplementary `P*` already wrong      | **0.30 too** |
| Symbols outside the BMP currency set            | Sm/Sk/So untested by the spec suite; emoji and maths symbols affected        | 0.31       |
| DEL (U+007F) not excluded from link destinations | `EXCLUDED_0_TO_SPACE` is `\u0000-\u0020`; `[a](b<DEL>c)` still links         | 0.30, **unconfirmed** |

The DEL item is deliberately marked unconfirmed: the spec text excludes U+007F, but it was **not** verified
that the reference implementations actually reject it. Confirm against commonmark.js before acting on it.

### Method and open points

Unchanged: TDD red first, one commit per concern, full suite green, a review sub-agent (max 2 rounds) with a
`review-B312.md`, and the baseline shrinking to zero. New behaviour becomes the `DataKey` default and
`COMMONMARK_0_30` and older opt out — note this means extending the existing opt-out condition to
`this != COMMONMARK_0_31_2 && this != COMMONMARK_0_30` for the 0.30 keys.

Proposed keys: `HTML_COMMENT_ANY_TEXT` (B312.1), `HTML_BLOCK_TAGS_SEARCH_NOT_SOURCE` (B312.2),
`UNICODE_PUNCTUATION_INCLUDES_SYMBOLS` (B312.3), each defaulting to the 0.31.2 behaviour.

Finally, as for 0.30: add `COMMONMARK_0_31_2` with its wiring, advance `COMMONMARK_LATEST` and `spec.txt`,
add `FullOrigSpec0312CoreTest` and a default-options variant, retire the baseline and `ComboOrigSpec0312CoreTest`,
update `VERSION.md`, and tag the milestone.

Two points need a decision before B312.3 starts:

1. **Is the supplementary-code-point fix gated or a plain bug fix?** It is wrong under 0.30 as well, so by the
   precedent set for the entity length it should be an **ungated bug fix** that also corrects the older
   profiles. Recommended.
2. **Should `INLINE_DELIMITER_DIRECTIONAL_PUNCTUATIONS` (the open/close variants) include symbols?** These are
   a flexmark extension point beyond the spec, so the spec does not answer it.

---

## Follow-up tasks (not blocking any version milestone)

### Task F2 — code-point-correct delimiter flanking (DONE)

Done, as an ungated bug fix. `InlineParserImpl.scanDelimiters` read the character before and after a delimiter
run as a single UTF-16 `char`, so a **supplementary** code point was inspected as a lone surrogate and
classified wrongly. Measured on JDK 21 (Unicode 15.0), **217 supplementary `P*` code points were misclassified**.

This was **not** a 0.31 change. CommonMark has always defined the flanking rules over Unicode characters, so it
was wrong under every profile flexmark supports, 0.26 through 0.30. It was therefore fixed **ungated**, by the
precedent set for the numeric character reference limits — gating it would have encoded the bug as though it
were intended behaviour.

The fix reads whole code points with `Character.codePointBefore` / `codePointAt` via a `codePointString`
helper that returns an identical string for BMP input, so BMP behaviour is provably unchanged. The existing
bounds handling already substituted the end-of-line sentinel before either call, so neither can read out of
range. `scanDelimiters` turned out to be the **only** site using the whitespace and punctuation matchers; the
extension delimiter processors consume only the resulting flanking booleans.

No spec example of any version covers supplementary code points, so `SupplementaryCodePointDelimiterTest`
(14 tests, run against the defaults and the 0.26, 0.29 and 0.30 profiles) is the only enforcement. RED proved
the bug on 4 of 12 initial cases, all supplementary punctuation. Review: `review-supplementary-code-points.md`.

**Known limitation:** there is no test under `INLINE_DELIMITER_DIRECTIONAL_PUNCTUATIONS`, which the fix does
touch. A trial showed even BMP punctuation renders differently in that mode, so any expectation would have
been a guess. Also untested: unpaired surrogates (reasoned to be unchanged). There are no supplementary
whitespace cases to test, as every `Zs` character is in the BMP.

**Deliberately excluded:** adding the Unicode `S*` categories to the punctuation set. That is Task B312.3.

### Task F3 — tests for the under-tested symbol punctuation rule (open, belongs with B312.3)


The 0.31.2 example set detects only **BMP currency symbols** (example 354), yet the change affects roughly
**7,978** code points. Sm, Sk and So are not exercised at all, and neither is `_` nor any supplementary
character. Per the standing rule, B312.3 must ship with hand-written tests well beyond example 354:

| Case to cover                        | Example input | Why it matters                              |
|--------------------------------------|---------------|---------------------------------------------|
| Sc currency beyond `£`/`€`           | `*¥*x`        | the only group the spec actually tests      |
| Sm mathematical                      | `*±*x`        | 930 BMP code points, untested               |
| Sk modifier                          | `*´*x`        | 118 BMP code points, untested               |
| So other, including emoji            | `a*😀*b`      | 2,731 BMP + 3,903 supplementary, untested   |
| the `_` delimiter for each of these  | `_±_x`        | stricter intraword rule, untested           |
| supplementary symbols                | U+1D6C1 etc.  | needs Task F2 to be correct first           |

These tests belong in the same commit series as B312.3 and depend on Task F2.

### Task F4 — exclude DEL (U+007F) from link destinations and autolinks (open, UNCONFIRMED)

`Parsing.EXCLUDED_0_TO_SPACE` is `\u0000-\u0020`, which excludes the C0 controls and space but **not** U+007F.
Probes show `[a](b<DEL>c)` and `<http://a<DEL>b>` both still produce links. The CommonMark definition of ASCII
control characters (0.30 onwards) includes U+007F, so the spec text says these should not be links.

**This is explicitly unconfirmed and must be verified before any code is changed.** It was *not* checked
whether commonmark.js and the other reference implementations actually reject U+007F here; the spec text and
the reference behaviour may diverge, and no spec example of any version covers it. Verify against
commonmark.js first. If the references do reject it, treat this as an ungated bug fix like Task F2 and add
hand-written tests, since no example enforces it.

### Task F1 — verify the exactness of the Unicode case folding (open)

B30.3 matches link labels with `Escaping.caseFold`, which lower-cases each code point, upper-cases the
string and lower-cases each code point again. That is a **Java approximation of Unicode full case folding,
not an implementation of it**. It handles the cases the spec cares about in practice (U+1E9E ẞ, ß,
ligatures, final sigma), and all 652 examples of `spec.0.30.txt` pass.

Measured against the current `CaseFolding.txt` (C and F entries), **163 of 1606 entries fold differently**.
They fall into three groups, only one of which can actually be wrong:

| Group                                                  | Harmful? |
|--------------------------------------------------------|----------|
| Different representative, same equivalence class (e.g. the Cherokee block) | no — both sides fold identically, so matching still succeeds |
| U+0130 LATIN CAPITAL LETTER I WITH DOT ABOVE           | possibly — needs checking |
| Characters newer than the JDK's Unicode version        | possibly — depends on the JDK in use |

**To do:** determine which of the 163 deviations genuinely break label matching (as opposed to merely
choosing a different representative, which is harmless), then decide between implementing real full case
folding driven by `CaseFolding.txt` and documenting the approximation as acceptable. Whatever is decided,
the limitation is already recorded in `VERSION.md` and `review-B30.md`.

No spec example detects this, so the shrink-only baseline cannot enforce it; it needs hand-written tests.

## Constraints I am operating under

- **Never push.** Commits only, on my branches.
- JDK 21+ required (`maven-enforcer`); `JAVA_HOME` must be set per invocation — the machine default is 17.
- Never silently regenerate extension spec resource files (analysis §7.4 guardrail 3).
- **I cannot query your Copilot plan usage** — no tool exposes it. I will track my own consumption and stop
  at the agreed scope boundary; please tell me if you see the limit approaching sooner.

Spec coverage (whole-file tests apply the profile shown, all run in `CoreRendererTestSuite`):

| Spec | Test class                          | Examples | Profile applied     | Status                          |
|------|-------------------------------------|----------|---------------------|---------------------------------|
| 0.26 | `FullOrigSpec026CoreTest`           | 618      | `COMMONMARK_0_26`   | passes                          |
| 0.27 | `FullOrigSpec027CoreTest`           | 622      | `COMMONMARK_0_27`   | passes                          |
| 0.28 | `FullOrigSpec028CoreTest`           | 624      | `COMMONMARK_0_28`   | passes                          |
| 0.29 | `FullOrigSpec029CoreTest`           | 649      | `COMMONMARK_0_29`   | passes                          |
| 0.29 | `FullSpec029DefaultOptionsCoreTest` | 649      | none (defaults)     | passes                          |
| 0.30 | `FullOrigSpec030CoreTest`           | 652      | `COMMONMARK_0_30`   | passes                          |
| 0.30 | `FullSpec030DefaultOptionsCoreTest` | 652      | none (defaults)     | passes                          |
| 0.30 | `FullOrigSpecCoreTest`              | 652      | `COMMONMARK_LATEST` | passes, guards `spec.txt` drift |
| 0.31.2 | `ComboOrigSpec0312CoreTest`       | 652      | none (defaults)     | passes with a 3-entry shrink-only baseline (354, 625, 626) |

`FullSpec029DefaultOptionsCoreTest` still passes under the 0.30 defaults: none of the four 0.30 changes
alters the rendering of any 0.29 example, so the older default-options test remains a valid regression guard.

