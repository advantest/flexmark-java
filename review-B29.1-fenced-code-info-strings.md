# Review — B29.1: info strings of fenced code blocks (CommonMark 0.29, example 116)

Reviewed commits: `37a80f099` *test(parser): pin info string rules of tilde and backtick code fences* and
`bff9d2a9a` *feat(parser): allow backticks in info strings of tilde code fences*.
Review round 1 of max. 2. Reviewer: `code-review` sub-agent (Claude Sonnet 5.5).

**Result: one real defect found in code touched indirectly, no defects in the change itself.**

## Confirmed correct

| Aspect                 | Finding                                                                                     |
| ---------------------- | ------------------------------------------------------------------------------------------- |
| Spec wording           | New pattern drops only the `(?!.*~)` lookahead; the backtick alternative is unchanged.      |
| Default preserves 0.28 | The new pattern is used only when the option is on; default is `false`.                     |
| Profile wiring         | Option is set only in the `COMMONMARK_0_29` branch of `setIn`; `COMMONMARK_LATEST` untouched. |
| Other fence detection  | No other main-code regex detects fence openers.                                             |
| Baseline               | Only entry 116 was removed; no `spec.*.txt` file was modified.                              |

## Applied

1. **Formatter could break a valid 0.29 fence.** With `FENCED_CODE_MARKER_TYPE = BACK_TICK`,
   `~~~ aa ``` ~~~` was rewritten with a backtick fence, which no longer parses as a fence.
   `CoreNodeFormatter` now keeps the tilde marker when the info string contains a backtick. Test written
   first and observed failing (round-trip HTML mismatch), then passing.
2. **Test quality.** Renamed the backtick-fence test so its name matches the profiles it covers, and added
   a test asserting the raw info string `aa ``` ~~~` and one asserting the option's default and profile.

## Declined

| Suggestion                                             | Reason                                                                   |
| ------------------------------------------------------ | ------------------------------------------------------------------------ |
| Drop the tab-trimming test as it already passes        | Kept on purpose: it pins the #505 changelog item, which needed no code change (`trim()` already strips tabs). |
| Test the backtick rule with the option on in a non-0.29 profile | Not reachable through a profile; the option is only set by the 0.29 profile. |

## Verification

- `FencedCodeInfoStringTest`: 7 tests, 0 failures (formatter test red without the fix).
- Full `mvn -o install`: BUILD SUCCESS.
