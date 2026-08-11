# Complying with the CommonMark Specification

Status document and work plan for bringing `flexmark-java` up to CommonMark 0.31.2, and for supporting
every specification version from 0.26 onwards via parser emulation profiles.

Written as context for follow-up work (human or agent). All numbers in this document were **measured**
against this repository at commit `bb4fc00a1` (`main`, version `0.65.3-SNAPSHOT`), not estimated from
reading the code.

---

## 1. Executive summary

`flexmark-java` is, in effect, a **CommonMark 0.28 implementation**. The default `spec.txt` bundled in
`flexmark-test-specs` is the 0.28 specification, and the parser passes it completely. Against the current
specification (0.31.2, released 2024-01-28) it fails **26 of 652** examples.

The five `COMMONMARK*` parser emulation profiles collapse into only **two** distinct behaviours.
`COMMONMARK_0_29` is a silent no-op alias for the 0.28 default.

CommonMark versions are **not** downward compatible. Supporting several versions therefore requires one
toggleable option per breaking change, which is the approach already used (successfully, but only for two
flags) for 0.26/0.27 versus 0.28.

Investigation additionally uncovered **two confirmed defects** in the profile machinery (§5.2, §5.3): the
CommonMark branch of `ParserEmulationProfile.setIn()` never applies its own list options, so the versioned
CommonMark profiles do not even record their own identity. These must be fixed *before* new profiles are
added, or the new profiles will be silently inert in exactly the same way. Fixing them, together with the
`COMMONMARK_LATEST` introduction, forms a self-contained behaviour-neutral first step (Task 0, §7.0).

## 1.1 Decisions taken

| # | Decision                                                                                                     | Status                        |
| - | ------------------------------------------------------------------------------------------------------------ | ----------------------------- |
| 1 | `COMMONMARK_0_26` means "the profile for specification version 0.26". Its `setEndOnDoubleBlank(true)` is therefore **a bug**. | Agreed — must be confirmed by a regression test before removal (§5.2) |
| 2 | Rename `COMMONMARK` → `COMMONMARK_LATEST` and make it the default, accepting the breaking change for downstream consumers. | Agreed — but see decisions 3 and 4, which together make the *initial* change behaviour-neutral |
| 3 | Keep `COMMONMARK` as the **family sentinel**; add `COMMONMARK_LATEST` as the **version profile**. (Option (a) of §5.4.) | Agreed |
| 4 | `COMMONMARK_LATEST` tracks the newest specification **implemented by that flexmark release**, not the newest published. Today that is **0.28**. Each advance is a deliberate, documented event recorded in `VERSION.md`. | Agreed |

**Consequence worth noting:** because `COMMONMARK_LATEST` ≡ 0.28 today and the current default already
*is* 0.28, decisions 2–4 taken together are a **behaviour-preserving rename** that can land immediately and
independently of Tasks B and C (§7.0). The breaking change promised in decision 2 does not arrive all at
once; it arrives incrementally, once per implemented specification version, and each step is opt-out-able by
pinning an explicit `COMMONMARK_0_2x` / `COMMONMARK_0_3x` profile.

---

## 2. Current state of the repository

| Item                                          | Status                                                                   |
| --------------------------------------------- | ------------------------------------------------------------------------ |
| `flexmark-test-specs/src/main/resources/spec.txt` | **CommonMark 0.28** (624 examples) — byte-identical to `spec.0.28.txt` |
| Bundled spec files                            | 0.26, 0.27, 0.28, 0.29, 0.30 — **0.31.2 is missing**                      |
| `FullOrigSpecCoreTest` (0.28)                 | enabled                                                                  |
| `FullOrigSpec027CoreTest`                     | enabled                                                                  |
| `FullOrigSpec028CoreTest`                     | enabled                                                                  |
| `FullOrigSpec029CoreTest`                     | **disabled** — returns `ResourceLocation.NULL`                            |
| Missing test classes                          | 0.26, 0.30, 0.31.2                                                       |
| `ParserEmulationProfile` constants            | `COMMONMARK_0_26/27/28/29`; no `0_30` / `0_31`                            |
| `COMMONMARK_0_28` / `COMMONMARK_0_29` branches | **empty stubs** with TODO comments                                       |
| CommonMark branch of `setIn()`                | **defective** — never calls `getOptions().setIn()`, so list options and profile identity are never applied (§5.2) |

`FullOrigSpec029CoreTest` carries the marker for this whole effort:

```java
@Override
protected ResourceLocation getSpecResourceLocation() {
    // FIX: implement 0.29 spec and enable test
    //return RESOURCE_LOCATION;
    return ResourceLocation.NULL;
}
```

### 2.1 Build prerequisite

The build enforces **JDK 21+** (`maven-enforcer-plugin`, `requireJavaVersion [21,)`). Building with JDK 17
fails immediately with:

```
Rule 1: RequireJavaVersion failed with message:
Detected JDK ... is version 17.0.19 which is not in the allowed range [21,).
```

Set `JAVA_HOME` to a JDK 21 (or newer) installation before building.

---

## 3. Measured conformance

Methodology: every official `spec.txt` from <https://spec.commonmark.org/> was parsed and each example
rendered with `Parser` + `HtmlRenderer` configured from the given `ParserEmulationProfile` plus
`HtmlRenderer.PERCENT_ENCODE_URLS = true` (mirroring `OrigSpecCoreTest`). Comparison is exact string
equality, matching what `FullSpecTestCase` does.

Cell value = **failures / total examples**.

| spec ＼ profile | `0_26`    | `0_27`    | `0_28`    | `0_29`    | `COMMONMARK` |
| --------------- | --------- | --------- | --------- | --------- | ------------ |
| **0.26**        | **0**/618 | **0**/618 | 3/618     | 3/618     | 3/618        |
| **0.27**        | **0**/622 | **0**/622 | 3/622     | 3/622     | 3/622        |
| **0.28**        | 3/624     | 3/624     | **0**/624 | **0**/624 | **0**/624    |
| 0.29            | 23/649    | 23/649    | 20/649    | 20/649    | 20/649       |
| 0.30            | 26/652    | 26/652    | 23/652    | 23/652    | 23/652       |
| 0.31.2          | 29/652    | 29/652    | 26/652    | 26/652    | 26/652       |

Three conclusions follow directly from this table.

1. **The top-left 3x3 block is a clean partition.** flexmark genuinely complies with 0.26, 0.27 and 0.28 —
   but only by switching profiles, never with a single configuration.
2. **There are only two distinct behaviours, not five.** Column `0_26` is identical to `0_27`, and columns
   `0_28`, `0_29` and `COMMONMARK` are identical to each other across all six specification versions.
3. **`COMMONMARK_0_29` is a silent no-op.** A caller selecting it today receives 0.28 behaviour with no
   error, no warning and no deprecation notice.

---

## 4. CommonMark versions are not downward compatible

This is the central constraint for all follow-up work.

Comparison of consecutive specification versions, matching examples by their **markdown input**:

| from → to      | shared md | same expected | **contradictory** | removed | added |
| -------------- | --------- | ------------- | ----------------- | ------- | ----- |
| 0.26 → 0.27    | 617       | 617           | **0**             | 0       | 5     |
| 0.27 → 0.28    | 618       | 615           | **3**             | 4       | 5     |
| 0.28 → 0.29    | 612       | 609           | **3**             | 11      | 36    |
| 0.29 → 0.30    | 644       | 644           | **0**             | 4       | 8     |
| 0.30 → 0.31.2  | 635       | 635           | **0**             | 17      | 17    |

"Contradictory" means identical markdown input for which the two specifications mandate *different* HTML:

```
0.27 → 0.28   ***foo***               <strong><em>foo</em></strong>   →  <em><strong>foo</strong></em>
0.27 → 0.28   _____foo_____           <strong><strong><em>…          →  <em><strong><strong>…
0.27 → 0.28   [link](foo(and(bar)))   literal text                   →  a link
0.28 → 0.29   ``` ```                 <code></code>                  →  <code> </code>
0.28 → 0.29   1. a / 2. b / 3. c      three list items               →  two items + indented code block
0.28 → 0.29   [link](</my uri>)       literal text                   →  <a href="/my%20uri">link</a>
```

### 4.1 The "contradictory" column understates the breakage

When the CommonMark committee changes behaviour it frequently **replaces** an example rather than editing
its expected output. Such changes appear as `removed` + `added`, not as `contradictory`.

The clearest case is 0.28 → 0.29, which reports only 3 contradictions but 11 removals. The removed examples
are precisely those whose semantics changed:

```
--- removed in 0.29: `` foo ` bar  ``
--- removed in 0.29: `foo   bar\n  baz`
--- removed in 0.29: `code  \nspan`
--- removed in 0.29: &#35; &#1234; &#992; &#98765432; &#0;
--- removed in 0.29: &nbsp &x; &#; &#x;\n&ThisIsNotDefined; &hi?;
--- removed in 0.29: [Foo bar]:\n<my%20url>\n'title'\n\n[Foo bar]
--- removed in 0.29: - a\n - b\n  - c\n   - d\n    - e\n   - f\n  - g\n - h\n- i
--- removed in 0.29: < a><\nfoo><bar/ >
```

**Do not use the `contradictory` count alone to size the work.** Always inspect removals as well.

### 4.2 Conversely, 0.30 → 0.31.2 overstates it

That transition reports 17 removals and 17 additions, but 15 of them are the *same* examples rewritten from
`http://` to `https://` (a cosmetic change from 0.31, spec issue #751):

```
--- removed in 0.31.2: <http://foo.bar.baz/test?q=hello&id=22&boolean>
+++ added   in 0.31.2: <https://foo.bar.baz/test?q=hello&id=22&boolean>
```

Only three are real:

```
+++ added in 0.31.2: foo <!-- this is a --\ncomment - with hyphens -->     (HTML comment syntax)
+++ added in 0.31.2: foo <!--> foo -->\n\nfoo <!---> foo -->               (HTML comment syntax)
+++ added in 0.31.2: *$*alpha.\n\n*€*bravo.\n\n*🍬*charlie.                 (Unicode symbols as punctuation)
```

### 4.3 Net breaking-change budget

| transition     | breaking changes | flags present today                                          |
| -------------- | ---------------- | ------------------------------------------------------------ |
| 0.26 → 0.27    | 0                | not needed                                                   |
| 0.27 → 0.28    | 2                | ✅ `STRONG_WRAPS_EMPHASIS`, `LINKS_ALLOW_MATCHED_PARENTHESES` |
| 0.28 → 0.29    | ~6 clusters      | ❌ none                                                       |
| 0.29 → 0.30    | 1                | ❌ none                                                       |
| 0.30 → 0.31.2  | 2                | ❌ none                                                       |

---

## 5. How 0.26/0.27/0.28 compliance is currently achieved

Two `DataKey`s carry the entire load, set in `ParserEmulationProfile.setIn()`:

```java
} else if (this == COMMONMARK_0_26 || this == COMMONMARK_0_27) {
    // set previous parsing rule options
    dataHolder.set(Parser.STRONG_WRAPS_EMPHASIS, true);
    dataHolder.set(Parser.LINKS_ALLOW_MATCHED_PARENTHESES, false);
} else if (this == COMMONMARK_0_28) {
    // set 0.28 parsing rule options
    // IMPORTANT: 0.28/0.29 differences
}
```

- `STRONG_WRAPS_EMPHASIS` handles the `***foo***` / `_____foo_____` nesting reversal (2 of the 3 contradictions).
- `LINKS_ALLOW_MATCHED_PARENTHESES` handles the nested-parenthesis link destination change (the 3rd).

### 5.1 Why it works

This works only because 0.26 → 0.28 broke exactly two small, cleanly isolatable things. It is nevertheless a
good template for the remaining work.

Note what this branch **does not** do: unlike every other branch in `setIn()`, it never calls
`getOptions(dataHolder).setIn(dataHolder)`. That omission is the root cause of both defects below.

### 5.2 CONFIRMED BUG — `COMMONMARK_0_26` list options are never applied

Per decision 1 (§1.1), `COMMONMARK_0_26` denotes the profile for specification version 0.26. In
`ParserEmulationProfile.getOptions()`:

```java
if (family == COMMONMARK) {
    if (this == COMMONMARK_0_26) {
        return new MutableListOptions((DataHolder) null).setEndOnDoubleBlank(true);
    }
    ...
}
```

The CommonMark 0.26 changelog states:

> Removed the "two blank lines breaks out of lists" rule. This is incompatible with the principle of
> uniformity […]

0.26 is the version that **dropped** the rule, so `setEndOnDoubleBlank(true)` requests *pre*-0.26 semantics.
That is defect number one.

Defect number two is more serious: **the value is never applied anyway.** Compare the branches of `setIn()`:

```java
if (this == FIXED_INDENT) {
    getOptions(dataHolder).setIn(dataHolder)      // <-- applies list options
            .set(Parser.STRONG_WRAPS_EMPHASIS, true)
            ...
} else if (this == KRAMDOWN) {
    getOptions(dataHolder).setIn(dataHolder);     // <-- applies list options
    ...
} else if (this == COMMONMARK_0_26 || this == COMMONMARK_0_27) {
    // NO getOptions(dataHolder).setIn(dataHolder) CALL
    dataHolder.set(Parser.STRONG_WRAPS_EMPHASIS, true);
    dataHolder.set(Parser.LINKS_ALLOW_MATCHED_PARENTHESES, false);
} else if (this == COMMONMARK_0_28) {
    // empty
}
```

`ListOptions.setIn()` is what actually writes the list keys **and** records the profile identity:

```java
public MutableDataHolder setIn(MutableDataHolder options) {
    options.set(Parser.PARSER_EMULATION_PROFILE, getParserEmulationProfile());
    ...
    options.set(Parser.LISTS_END_ON_DOUBLE_BLANK, endOnDoubleBlank);
```

Because the CommonMark branch never calls it, neither key is ever written.

**Measured evidence.** Resolving the keys after `new MutableDataSet().setFrom(profile)`:

```
selected=COMMONMARK_0_26   PARSER_EMULATION_PROFILE=COMMONMARK      LISTS_END_ON_DOUBLE_BLANK=false
selected=COMMONMARK_0_27   PARSER_EMULATION_PROFILE=COMMONMARK      LISTS_END_ON_DOUBLE_BLANK=false
selected=COMMONMARK_0_28   PARSER_EMULATION_PROFILE=COMMONMARK      LISTS_END_ON_DOUBLE_BLANK=false
selected=COMMONMARK        PARSER_EMULATION_PROFILE=COMMONMARK      LISTS_END_ON_DOUBLE_BLANK=false
selected=FIXED_INDENT      PARSER_EMULATION_PROFILE=FIXED_INDENT    LISTS_END_ON_DOUBLE_BLANK=false
selected=KRAMDOWN          PARSER_EMULATION_PROFILE=KRAMDOWN        LISTS_END_ON_DOUBLE_BLANK=false
```

`LISTS_END_ON_DOUBLE_BLANK` is `false` for `COMMONMARK_0_26` despite `getOptions()` requesting `true`, and
**no versioned CommonMark profile records its own identity** — all three report plain `COMMONMARK`. The
non-CommonMark families record theirs correctly.

Confirmed end-to-end: rendering markdown designed to trigger the rule gives byte-identical output for
`COMMONMARK_0_26`, `COMMONMARK_0_27`, `COMMONMARK_0_28` and `COMMONMARK`:

```
md: "- a\n\n\n- b\n"   →  all four profiles: <ul><li><p>a</p></li><li><p>b</p></li></ul>
md: "- a\n\n  b\n\n\n  c\n" → all four profiles: <ul><li><p>a</p><p>b</p><p>c</p></li></ul>
```

This is also why the `0_26` and `0_27` columns of the §3 matrix are identical, and why no spec example ever
caught it.

#### Required regression test (do this before changing anything)

The bug is invisible to the spec suites, so it needs a dedicated test. Add to `flexmark-core-test` a test
that asserts on **options resolution**, not only on rendered output — rendering alone cannot distinguish
"flag correctly false" from "flag never applied":

1. For every profile, assert `Parser.PARSER_EMULATION_PROFILE.get(options)` equals the selected profile.
   This currently **fails** for `COMMONMARK_0_26/27/28/29` and is the test that pins the fix.
2. Assert `Parser.LISTS_END_ON_DOUBLE_BLANK.get(options) == false` for `COMMONMARK_0_26`, encoding
   decision 1 — 0.26 removed the rule.
3. Add rendering cases for `- a\n\n\n- b\n` and `- a\n\n  b\n\n\n  c\n` under each CommonMark profile, so
   the behaviour is pinned once the options actually reach the parser.
4. Only then remove the `setEndOnDoubleBlank(true)` and add the missing
   `getOptions(dataHolder).setIn(dataHolder)` call.

**Do steps 1–3 first and watch step 1 fail.** A test written after the fix proves nothing.

### 5.3 Hidden coupling — fixing §5.2 will change extension behaviour

`flexmark-ext-definition` compares the **profile** against the `COMMONMARK` *family sentinel* in two places,
where it should compare `.family`:

```java
// DefinitionItemBlockParser.java:116
if (!hasContent || options.myParserEmulationProfile == COMMONMARK && contentOffset > options.newItemCodeIndent) {

// DefinitionItemBlockParser.java:292-295
ParserEmulationProfile emulationFamily = options.myParserEmulationProfile;   // NOT .family, despite the name
int codeIndent = emulationFamily == COMMONMARK || emulationFamily == FIXED_INDENT ? options.codeIndent : options.itemIndent;
```

Line 137 of the same file does it correctly (`options.myParserEmulationProfile.family`).

These two comparisons work **by accident today**, precisely because of the §5.2 bug: `PARSER_EMULATION_PROFILE`
always resolves to `COMMONMARK`, so `== COMMONMARK` is true even when the user selected `COMMONMARK_0_26`.

⚠️ **The moment §5.2 is fixed, these comparisons start returning `false` for versioned profiles and
definition-list parsing silently changes.** Fix all three sites together, in one commit, with definition-list
tests in place beforehand. This is the single most likely source of a surprise regression in this whole
effort.

### 5.4 Renaming `COMMONMARK` — resolved design

Per decision 2 (§1.1), `COMMONMARK` is to be renamed `COMMONMARK_LATEST` and become the default. A
complication had to be settled first: **`COMMONMARK` currently serves double duty.**

1. It is the *latest-version profile* — the default value of `Parser.PARSER_EMULATION_PROFILE`.
2. It is the *family sentinel* — every versioned constant is declared `COMMONMARK_0_2x(COMMONMARK)`, and code
   across the project branches on `emulationFamily == COMMONMARK`.

A blind rename yields `emulationFamily == COMMONMARK_LATEST`, which is actively misleading: a document parsed
as 0.26 does belong to the CommonMark *family*, but is emphatically not "latest".

**Resolved (decision 3): keep `COMMONMARK` as the family sentinel, add `COMMONMARK_LATEST` as the version
profile.** This removes the ambiguity that produced the §5.3 bug.

Affected sites (small and tractable — the rename itself is cheap):

| File                                     | Occurrences | Kind                                |
| ---------------------------------------- | ----------- | ----------------------------------- |
| `ParserEmulationProfile.java`            | 6           | declarations + family checks        |
| `Parser.java`                            | 1           | default of `PARSER_EMULATION_PROFILE` |
| `ListBlockParser.java`                   | 1           | family check                        |
| `ListItemParser.java`                    | 1           | family check                        |
| `DefinitionItemBlockParser.java`         | 3           | 1 family check + 2 buggy profile checks (§5.3) |
| `ComboCommonMarkCompatibilitySpecTest.java` | 1        | test setup                          |
| `README.md`                              | 2           | documentation                       |

#### `COMMONMARK_LATEST` must be a static field, not an enum constant

This follows directly from decision 4 and is the single most important implementation detail in this section.

Every consumer of `PARSER_EMULATION_PROFILE` in the codebase compares with **`==` identity**, and a repository
search found **no** name-based lookup (`ParserEmulationProfile.valueOf(...)`, `values()`, `.name()`) of this
enum anywhere. Therefore:

```java
// CORRECT — alias, identical object, == comparisons keep working
public enum ParserEmulationProfile {
    COMMONMARK(null),            // family sentinel, not selectable as a version
    COMMONMARK_0_26(COMMONMARK),
    ...
    COMMONMARK_0_28(COMMONMARK);

    /** Newest CommonMark specification implemented by this flexmark release. */
    public static final ParserEmulationProfile COMMONMARK_LATEST = COMMONMARK_0_28;
}
```

```java
// WRONG — a separate enum constant has its own identity
COMMONMARK_LATEST(COMMONMARK),   // duplicates 0.28 config; breaks
                                 // PARSER_EMULATION_PROFILE.get(o) == COMMONMARK_0_28
```

A Java enum constant cannot alias another enum constant, so a `static final` field is the only construction
that preserves identity. Advancing the alias then becomes a **one-line change** per implemented version.

Known caveats of the field approach, all acceptable:

- `COMMONMARK_LATEST` will not appear in `values()` and `valueOf("COMMONMARK_LATEST")` will throw. No internal
  code does this, but it is public API — provide a small name-resolution helper if configuration-by-string is
  ever needed.
- `COMMONMARK_LATEST.name()` returns `"COMMONMARK_0_28"`. This is a **feature**: any serialised configuration
  or debug output records the concrete specification version rather than a moving label, so persisted
  configuration stays reproducible across flexmark upgrades.

#### Advancement protocol

`COMMONMARK_LATEST` advances **only** when a specification version is actually implemented and its
`FullOrigSpec0xxCoreTest` passes at zero failures. It must never point at a version that is merely published.

Invariants to enforce with tests:

1. `COMMONMARK_LATEST` is always identical (`==`) to some concrete `COMMONMARK_0_xx` constant.
2. The `FullOrigSpec0xxCoreTest` for that version passes with zero failures.
3. `flexmark-test-specs/src/main/resources/spec.txt` is byte-identical to `spec.0.xx.txt` for that same
   version. This already holds today (both are 0.28) and keeps the default test resource honest.
4. `Parser.PARSER_EMULATION_PROFILE`'s default equals `COMMONMARK_LATEST`.

Each advance requires a `VERSION.md` entry stating the old and new specification version, that the default
parser behaviour has changed, and that consumers needing the previous behaviour should pin the explicit
`COMMONMARK_0_xx` profile. Per decision 4 this is a deliberate, visible event — never a silent side effect of
upgrading flexmark.

---

## 6. The 26 failures against CommonMark 0.31.2

Grouped by root cause. Example numbers are 1-based indices into `spec.0.31.2.txt`.

| # | Cluster                          | Examples                     | Count | Spec change                                                       | Likely code area                        |
| - | -------------------------------- | ---------------------------- | ----- | ----------------------------------------------------------------- | --------------------------------------- |
| 1 | Code span normalization          | 331, 332, 334–337, 640       | 7     | 0.29 (#532, #569): do not collapse interior spaces; strip only one leading+trailing space, and not if all spaces; newline → space | `InlineParserImpl` code-span handling |
| 2 | Link destination in `<…>`        | 195, 201, 215, 216, 489, 493 | 6     | 0.29 (#503, #538, #562): allow spaces inside `<>`; disallow bare leading `<`; handle `\>` | inline link parsing, `ReferenceRepository` |
| 3 | Emphasis multiple-of-3 rule      | 416, 417                     | 2     | 0.29 (#528): match interior runs when *both* lengths are multiples of 3 | `EmphasisDelimiterProcessor`        |
| 4 | Unicode punctuation set          | 354                          | 1     | 0.31: symbols added to Unicode punctuation                        | delimiter flanking-run classifier       |
| 5 | List items indented 4+ spaces    | 312, 313                     | 2     | 0.29 (#497): become continuation lines or indented code           | `ListBlockParser`                       |
| 6 | `<textarea>` as literal HTML     | 171                          | 1     | 0.30 (#657, #667): literal block tag like `pre`/`script`/`style`   | `HtmlBlockParser`                       |
| 7 | HTML comment syntax              | 625, 626                     | 2     | 0.31: match the HTML spec — allow `<!-->`, `<!--->`, inner `--`    | raw-HTML inline matcher                 |
| 8 | Fenced code info strings         | 138, 146                     | 2     | 0.29 (#119, #505): backticks allowed after tilde fences; trim tabs | `FencedCodeBlockParser`                 |
| 9 | Entity limits + ref case folding | 28, 540                      | 2     | 0.29 (#487) 6 hex / 7 decimal digit cap; (#582) full Unicode case fold (`ẞ` ↔ `SS`) | entity decoder, reference normalizer |

Note that **clusters 1, 2, 3, 5, 8 and 9 all originate in 0.29** — that single version is the bulk of the work.

### 6.1 Representative diffs

```
#28  Entity and numeric character references
     md:       &#87654321;
     expected: <p>&amp;#87654321;</p>          (over the 7-digit cap → literal)
     actual:   <p>?</p>

#138 Fenced code blocks / code spans
     md:       ``` ```
     expected: <p><code> </code>\naaa</p>
     actual:   <p><code></code>\naaa</p>

#171 HTML blocks
     md:       <textarea>\n\n*foo*\n\n</textarea>
     expected: contents kept literal
     actual:   <p><em>foo</em></p>              (markdown parsed inside textarea)

#195 Link reference definitions
     md:       [Foo bar]:\n<my url>\n'title'\n\n[Foo bar]
     expected: <p><a href="my%20url" title="title">Foo bar</a></p>
     actual:   not recognised as a definition at all

#312 Lists
     md:       - a\n - b\n  - c\n   - d\n    - e
     expected: last item is "d\n- e" (4-space indent = continuation)
     actual:   five separate list items

#416 Emphasis
     md:       foo***bar***baz
     expected: <p>foo<em><strong>bar</strong></em>baz</p>
     actual:   <p>foo***bar***baz</p>          (no emphasis at all)

#493 Links
     md:       [link](<foo\>)
     expected: <p>[link](&lt;foo&gt;)</p>
     actual:   <p><a href="foo%5C">link</a></p>

#540 Links (Unicode case folding)
     md:       [ẞ]\n\n[SS]: /url
     expected: <p><a href="/url">ẞ</a></p>
     actual:   <p>[ẞ]</p>

#625 Raw HTML
     md:       foo <!-- this is a --\ncomment - with hyphens -->
     expected: passed through as raw HTML
     actual:   escaped as text

#640 Hard line breaks
     md:       `code  \nspan`
     expected: <p><code>code   span</code></p>  (two spaces preserved + newline → space)
     actual:   <p><code>code span</code></p>
```

---

## 7. Work plan

### 7.0 Task 0 — profile machinery fixes and rename (behaviour-neutral)

Effort: **2–4 days**. **Do this first.** It unblocks everything else and, critically, changes no rendering
behaviour, so it can be reviewed and merged on its own.

1. Write the options-resolution regression test from §5.2 and **watch assertion 1 fail** for
   `COMMONMARK_0_26/27/28/29`.
2. Add definition-list tests covering the `DefinitionItemBlockParser` paths at lines 116 and 292–295, so the
   §5.3 coupling is pinned before it is disturbed.
3. In one commit: add the missing `getOptions(dataHolder).setIn(dataHolder)` call to the CommonMark branch of
   `setIn()`, remove `setEndOnDoubleBlank(true)` from `COMMONMARK_0_26`, **and** fix the two profile-vs-family
   comparisons in `DefinitionItemBlockParser`. These must move together (§5.3).
4. Keep `COMMONMARK` as the family sentinel; add `public static final ParserEmulationProfile
   COMMONMARK_LATEST = COMMONMARK_0_28;` (§5.4). Point `Parser.PARSER_EMULATION_PROFILE`'s default at it.
5. Add the four invariant tests from §5.4 ("advancement protocol").
6. Note the rename in `VERSION.md`, stating that behaviour is unchanged at this point.

Expected outcome: the §3 matrix is **completely unchanged**. Any cell that moves indicates an unintended
behaviour change — most likely from step 3 — and must be investigated, not accepted.

### 7.1 Task A — test coverage for all specification examples

Effort: **0.5–1 day**. Purely mechanical, no parser changes, no risk.

1. Add `flexmark-test-specs/src/main/resources/spec.0.31.2.txt`, downloaded from
   <https://spec.commonmark.org/0.31.2/spec.txt>. It already uses the ` ```````````````````````````````` example `
   fence format that the existing harness parses.
2. Add `FullOrigSpec026CoreTest`, `FullOrigSpec030CoreTest`, `FullOrigSpec0312CoreTest` — roughly 20 lines
   each, copied from `FullOrigSpec029CoreTest`.
3. Register all of them in `CoreRendererTestSuite`.
4. Decide how to land the currently-red tests. Options, in order of preference:
   - a known-failures list keyed by example number, asserted to shrink and never grow;
   - keep them disabled behind `ResourceLocation.NULL` as `029` is today (least useful — this is exactly how
     the 0.29 gap went unnoticed);
   - commit an "accepted actual output" baseline (`FullSpecTestCase` already writes an *actual* file).

Consider driving the tests from `spec.json` instead of `spec.txt`. The JSON carries `example`, `section` and
`start_line` fields, which lets failures be reported with their upstream example number and section — far
better diagnostics. Cost: a JSON dependency in the test module.

Useful resources per version (substitute `0.31.2`):

- `https://spec.commonmark.org/0.31.2/spec.txt` — the specification, examples included
- `https://spec.commonmark.org/0.31.2/spec.json` — machine-readable test cases
- `https://spec.commonmark.org/changelog.txt` — **the best single source**; maps every behavioural change to
  its upstream issue number
- `https://spec.commonmark.org/0.31.2/changes.html` — a raw full-text diff; verbose and of little practical use

### 7.2 Task B — comply with CommonMark 0.31.2

Effort: **15–30 developer-days** for someone fluent in the codebase; **6–10 weeks** for a newcomer.

The parser edits themselves are not the cost. There are only nine root causes, and clusters 4, 6, 7 and 9
(6 examples) are close to trivial. The cost is blast radius: roughly 60 modules, and every extension carries
committed spec/AST resource files that encode today's 0.28 behaviour. Any change to code spans, emphasis or
list indentation regenerates dozens of those files, and each diff must be reviewed by hand to separate
"correct new behaviour" from "regression".

Suggested order — cheapest and least coupled first:

1. Cluster 6 (`textarea`) — additive, one tag in a list.
2. Cluster 9 (entity digit cap, reference case folding) — self-contained.
3. Cluster 7 (HTML comment syntax) — one matcher.
4. Cluster 8 (info strings) — localised to `FencedCodeBlockParser`.
5. Cluster 4 (Unicode punctuation) — a character-class table update.
6. Cluster 1 (code spans) — moderate; complicated by `BasedSequence` slicing, so whitespace handling is not a
   plain string operation.
7. Cluster 2 (link destinations) — moderate; touches both inline links and reference definitions.
8. Cluster 3 (emphasis multiple-of-3) — hard; interacts with `STRONG_WRAPS_EMPHASIS`.
9. Cluster 5 (list indent) — **hardest, schedule this last**.

**Primary risk: cluster 5.** `ListBlockParser` plus `ListOptions` is the most option-laden code in the
project; `MarkdownProcessorsEmulation.md` documents roughly 30 interacting list flags. A change here affects
every emulation family (`FIXED_INDENT`, `KRAMDOWN`, `MARKDOWN`, `GITHUB`, `PEGDOWN`, …), not just CommonMark.

**Secondary risk: cluster 1.** Code span content is a `BasedSequence` view over the source, so "strip one
leading space" is a slicing operation with implications for source-position tracking, which the formatter and
`flexmark-html2md-converter` depend on.

### 7.3 Task C — support every version from 0.26 to 0.31.2 as profiles

Effort: **Task B + 10–20 developer-days**, so roughly **8–14 weeks** in total.

The trap: 0.26, 0.27 and 0.28 pass today only *because the default parser is 0.28*. The moment the default
moves to 0.31.2, all three currently-green profiles break, and each of the nine changes must become a
toggleable option.

Required work:

0. **Prerequisite:** complete Task 0 (§7.0) — the profile machinery fixes and the `COMMONMARK_LATEST`
   introduction. Adding profiles on top of the current machinery would produce more silently-inert profiles.
1. Roughly **11 new `DataKey`s** — code span normalization mode, link destination space policy, entity digit
   cap, `textarea`-literal, HTML comment syntax variant, info string policy, list indent policy, emphasis
   rule variant, reference case-fold mode, and so on.
2. New enum constants `COMMONMARK_0_30` and `COMMONMARK_0_31`.
3. Fill in the empty `COMMONMARK_0_28` and `COMMONMARK_0_29` branches in both `getOptions()` and `setIn()`.
4. Advance `COMMONMARK_LATEST` one version at a time, per the protocol in §5.4 — only after that version's
   `FullOrigSpec0xxCoreTest` reaches zero failures, and with a `VERSION.md` entry for each step.
5. Test matrix of 6 specification versions x 7 profiles. **The diagonal must be zero.** That invariant is the
   regression harness for the whole effort and it is an unusually strong one.
6. Extend the §5.2 options-resolution test to every new profile, so that no profile can ever again be added
   without actually being wired up.

Because of decision 4, this task delivers value incrementally: each implemented version is a shippable
release that advances `COMMONMARK_LATEST` by one step, rather than a single large breaking jump from 0.28
to 0.31.2.

Two changes are **not** cleanly expressible as booleans:

- **Emphasis** needs a three-valued setting, not a flag: pre-0.26 (no multiple-of-3 rule), 0.26–0.28
  (original rule), 0.29+ (refined rule, both runs multiples of 3).
- **List indent** is a block-structure decision, not a rendering tweak; it cannot be deferred to the renderer.

Open product decision, now settled (decisions 2–4, §1.1): `COMMONMARK` remains the family sentinel, a new
`COMMONMARK_LATEST` static field becomes the default version profile, and it tracks the newest specification
*implemented by that flexmark release* — 0.28 today. Given that upstream `vsch/flexmark-java` is effectively
unmaintained and this fork is where the work lands, this is entirely our call. §5.4 records the resolved
design, why `COMMONMARK_LATEST` must be a static field rather than an enum constant, and the advancement
protocol with its `VERSION.md` requirement.

Versions 0.25 and earlier use the older `.`-delimited example format and would need a second spec parser.
Recommend explicitly scoping them out.

### 7.4 Task D — autonomous agent loop

Setup effort: **3–5 days**. Realistic outcome: closes roughly 70% of Task B autonomously, then stalls.

This problem suits an agent loop unusually well because it has a **perfect, cheap, unambiguous oracle**:
652 examples with exact expected output, plus a hard invariant (0.26/0.27/0.28 must stay at 618/622/624).

Components to build:

1. **Conformance harness** emitting per-example JSON (version, example number, section, pass/fail, expected,
   actual). A throwaway version took about 120 lines; productionising it as a Maven profile is roughly a day.
2. **Two-speed loop.** Fast inner loop = harness only, about 5 seconds. Slow outer loop = full `mvn test`
   across ~60 modules, minutes. The agent must not run the slow loop every iteration or it will make no
   progress.
3. **Acceptance gate.** A patch is kept only if: target failures decrease, **and** no other specification
   version regresses, **and** `git diff` touches zero extension spec resources without an explicit,
   separately-reviewed regeneration step.
4. **Task queue** seeded from the cluster table in §6 and grounded in `changelog.txt`, which maps every
   behavioural change to its upstream issue number.

Where it will succeed: clusters 4, 6, 7, 8 and 9 (about 8 examples) are localised and self-evidently correct
once fixed.

Where it will stall: clusters 1, 2, 3 and 5 are tightly-coupled state machines where a naive fix trades one
failing example for another. Expect thrashing.

**The single most important guardrail is number 3.** An agent will otherwise "fix" failures by regenerating
extension spec resource files, silently baking in regressions across the whole project. Everything else in
the design is negotiable; this is not.

Realistic expectation: roughly 640–645 of 652 hands-off, then human judgement is needed for the
parser-architecture calls. This is a good candidate for a supervised loop with review checkpoints, not a
fire-and-forget project.

---

## 8. Reproducing the measurements

There is no committed harness yet — building one is Task D step 1. The measurements above were produced with
a standalone class that:

1. parses each `spec.<version>.txt` by scanning for lines beginning with 32 backticks followed by `example`,
   reading markdown until a line consisting solely of `.`, then expected HTML until the closing fence;
2. replaces `→` (U+2192) with a tab character — the specification uses it to visualise tabs, and failing to
   do this produces dozens of spurious failures;
3. renders with `Parser` + `HtmlRenderer` built from `ParserEmulationProfile.<PROFILE>` plus
   `HtmlRenderer.PERCENT_ENCODE_URLS = true`;
4. compares with exact string equality.

Pitfalls worth knowing before rebuilding it:

- **Set `JAVA_HOME` to JDK 21+**, otherwise the enforcer plugin fails the build (§2.1).
- **The `→` substitution is mandatory.** Without it the tab-handling section fails wholesale and masks the
  real results.
- **Match examples by markdown input, not by example number,** when diffing specification versions against
  each other. Numbering shifts whenever an example is inserted, so number-based matching produces noise.
- **Always report removals and additions alongside contradictions** (§4.1), or the size of the 0.28 → 0.29
  change will be badly underestimated.
- `HtmlRenderer.PERCENT_ENCODE_URLS = true` is required to match `OrigSpecCoreTest`; without it the link
  examples fail for unrelated reasons.
- **Rendering output alone cannot prove a profile is wired up.** As §5.2 shows, a profile can request an
  option that never reaches the parser, and the rendered output is then indistinguishable from the option
  being correctly disabled. Assert on resolved `DataKey` values via
  `Parser.<KEY>.get(new MutableDataSet().setFrom(profile))` as well.

---

## 9. Reference

- Specification index and all versions: <https://spec.commonmark.org/>
- Current version, 0.31.2 (2024-01-28): <https://spec.commonmark.org/0.31.2/>
- Changelog covering every version: <https://spec.commonmark.org/changelog.txt>
- Upstream specification repository: <https://github.com/commonmark/commonmark-spec/>

Key files in this repository:

| Path                                                                                     | Role                                  |
| ---------------------------------------------------------------------------------------- | ------------------------------------- |
| `flexmark/src/main/java/com/vladsch/flexmark/parser/ParserEmulationProfile.java`          | profile definitions and option wiring |
| `flexmark-test-specs/src/main/resources/spec*.txt`                                        | bundled specification files           |
| `flexmark-core-test/src/test/java/com/vladsch/flexmark/core/test/util/renderer/`          | spec test classes and suites          |
| `MarkdownProcessorsEmulation.md`                                                          | documents the list-option matrix      |
