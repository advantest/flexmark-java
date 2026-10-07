# Review — Task A: CommonMark 0.29 per-example spec tests

Reviewed commit: `72753b840` *test(core): cover CommonMark 0.29 spec per example with known-failures baseline*.
Review round 1 of max. 2. Reviewer: `code-review` sub-agent (Claude Sonnet 5.5).

**Result: no defects.** The commit is test-only as claimed; the numbering and the option set are sound.
Three improvements were accepted and applied in a follow-up commit.

## Confirmed correct

| Aspect                         | Finding                                                                                     |
| ------------------------------ | ------------------------------------------------------------------------------------------- |
| Global example numbering       | `TestUtils.getTestData` emits one full-spec entry first, then examples in file order. A running count therefore equals the spec's own numbering. 649 `example` fences counted in `spec.0.29.txt`. |
| `isFullSpecExample()` filter   | Correct. That entry would otherwise run the whole spec with the wrong options. `FullOrigSpec029CoreTest` covers the full file. |
| Drift detection                | Loud, not silent. Any shift either unmarks a failing example or marks a passing one; both fail. |
| Option set                     | Matches `OrigSpecCoreTest`. `INDENT_SIZE=0` is required because `RendererSpecTest` defaults it to 2, while `HtmlRenderer`'s own default — used by the orig-spec tests — is 0. |
| Test-only claim                | Holds. Nothing outside `flexmark-core-test` changed; `FullOrigSpec029CoreTest` still returns `ResourceLocation.NULL`. |

## Applied improvements

1. **`withOptionsSet` replaces rather than merges.** `spec.0.29.txt` declares no per-example options, so
   nothing was lost, but copying the pattern to a spec file that does declare options would silently drop
   them. Now merged via `withFailOption(...)`, which appends `FAIL` to any existing comma-separated set.
2. **Stale baseline entries were undetectable.** A typo such as `3310` silently marked nothing. `data()`
   now calls `assertKnownFailuresExist(...)` and throws listing any entry outside `1..exampleCount`.
3. **Mapping a failing test to a baseline entry was manual.** Test names are section-relative
   (`Code spans: 4`) while the baseline uses global numbers. The baseline header now states the naming
   scheme, notes that entries are grouped by section, and explains that a non-`ComparisonFailure` result
   means the example started to crash rather than to mis-render.

## Declined

| Suggestion                                            | Reason                                                                    |
| ----------------------------------------------------- | ------------------------------------------------------------------------- |
| Assert the example total is exactly 649                | The range guard already catches a truncated spec, and a hard-coded total would have to be maintained for every future spec file. |
| Better message for a malformed baseline line           | A `NumberFormatException` in the static initializer already fails loudly, and the offending file is named in the stack trace. |
| Custom `@Parameterized` name carrying the global number | Would need the number threaded through the constructor or a parallel list; the documented mapping is cheaper and local. |

## Notes for later, not acted on

- `CoreRendererSpecTest` sets `OBFUSCATE_EMAIL_RANDOM=false`, which `OrigSpecCoreTest` does not. No 0.29
  example uses email obfuscation, so this has no effect here.
- `wantExampleInfo()` differs from `OrigSpecCoreTest`; it only affects an informational comment in the
  rendered output.
- The cluster comments in the baseline file are a provisional reading of the example sources, **not**
  verified root causes. The file header says so. Confirm the cause before implementing any fix.

## Verification

- `ComboOrigSpec029CoreTest`: 649 tests, 0 failures — before and after the applied improvements.
- Ratchet proven: empty baseline → 20 failures; full baseline → 0; removing only entry `331` →
  exactly 1 failure (`Code spans: 4`).
- Full `mvn -o install`: BUILD SUCCESS, 60 modules.
