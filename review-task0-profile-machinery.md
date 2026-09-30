# Review: Task 0 profile machinery (e2a1c8dbb, 1ba0913fb, cce4b2e91, dea031b8a)

## Summary

No behaviour change or correctness bug found. Both claimed defects are real and correctly fixed, the
0.26 `endOnDoubleBlank` inversion is correctly removed, and the default change to `COMMONMARK_0_28` is
behaviour-neutral for every consumer in the repository. Two small hardening edits were applied (a code comment
and two stronger tests). Nothing was committed.

Verified by reading the whole repository (`git grep` over all `*.java`, all modules including extensions and
samples) for `PARSER_EMULATION_PROFILE`, `FORMATTER_EMULATION_PROFILE`, `getParserEmulationProfile`,
`myParserEmulationProfile`, `emulationProfile` and `COMMONMARK`.

## Findings

| # | Severity | Finding | Status |
|---|----------|---------|--------|
| 1 | Info | No remaining `profile == COMMONMARK` (family sentinel) comparisons. All other consumers use `.family` (`ListBlockParser`, `ListItemParser`, `TocUtils`, `FormatterOptions`, `DefinitionItemBlockParser` l.137) or compare to non-family profiles (`GITHUB_DOC` in `ListItemParser`, `CoreNodeFormatter`). | Verified |
| 2 | Info | `emulationProfile == FIXED_INDENT` asymmetry in `DefinitionItemBlockParser` is correct. Old code compared the profile to `FIXED_INDENT`, which is true only for the bare `FIXED_INDENT` profile; `MULTI_MARKDOWN`/`PEGDOWN*` resolve to themselves (they were already tagged) and were false. Switching to `.family` would flip them to true. Keeping the profile comparison is strictly neutral. It was unexplained in code. | Applied: comment added |
| 3 | Low | Test `everyProfileResolvesItsListOptions` only compared `endOnDoubleBlank`, so a `setIn` writing a wrong or partial option set would pass. | Applied: added `everyProfileResolvesAllOfItsListOptions` (full `ListOptions.equals`) |
| 4 | Low | No test proved the neutrality claim that unconfigured defaults equal the new default profile's options. | Applied: added `defaultListOptionsEqualTheCommonMarkLatestProfileOptions` |
| 5 | Info | Static init order is safe. Enum constants are initialised first, then static fields in textual order, so `COMMONMARK_LATEST = COMMONMARK_0_28` is non-null. `Parser` -> `ParserEmulationProfile` is a one-way dependency at class-init time (the enum only touches `Parser` keys inside methods). `PEGDOWN_EXTENSIONS` was already a static in the same enum. | No action |
| 6 | Info | `commonMarkLatestMatchesTheDefaultSpecificationResource` is intentionally a tripwire (asserts the alias equals 0.28 and `spec.txt` bytes match `spec.0.28.txt`). It is not tautological for its purpose, but it will need editing on every advance. | No action |

## Declined suggestions with rationale

- Changing the `FIXED_INDENT` half to `.family`: would change behaviour for MULTI_MARKDOWN and PEGDOWN*
  (out of scope for Task 0). Left as is; possibly a latent upstream bug, worth a separate decision later.
- Making `COMMONMARK_LATEST` an enum constant: cannot alias; would break `==` identity checks. Agree with the
  static field.
- Deduplicating the trailing `return ...setParserEmulationFamily(this)` / dead `else if (this == COMMONMARK_0_28)`
  empty branches: style only.

## Residual risks (for a human)

1. **Overwrite semantics of `setIn`.** COMMONMARK-family `setIn` now writes all list options (previously nothing).
   Code that sets a list option and afterwards calls `setFrom(ParserEmulationProfile.COMMONMARK*)` will now have
   it overwritten, as already happens for every other profile. Inside this repo the tests pass, but external
   users may notice.
2. **Default value observable change.** `Parser.PARSER_EMULATION_PROFILE.get(...)` and
   `FORMATTER_EMULATION_PROFILE` now return `COMMONMARK_0_28` instead of `COMMONMARK`. External code doing
   `== COMMONMARK` on the profile (rather than `.family`) will now be false; likewise
   `ListOptions.equals/hashCode` include the profile, so a default-constructed `ListOptions` no longer equals
   `COMMONMARK.getOptions()`. Binary compatible, but a source/semantic compatibility note for VERSION.md.
3. **Versioned profiles now record themselves.** External code that expected the family sentinel from a
   versioned profile (`COMMONMARK_0_27.getOptions().getParserEmulationProfile() == COMMONMARK`) breaks; this
   is the intended fix.
4. **Direct key use.** A user setting `Parser.PARSER_EMULATION_PROFILE` to a versioned profile directly (not via
   `setFrom`) previously fell through the definition extension's `== COMMONMARK` checks; it now takes the
   CommonMark path (correct, but a change for that unusual configuration).
5. `COMMONMARK_LATEST` is not a real enum value, so `valueOf("COMMONMARK_LATEST")`, `name()`-based config,
   switch statements and serialisation cannot see it. No such usage exists in this repo.

## Changes applied in the working tree (uncommitted)

- `flexmark-ext-definition/.../DefinitionItemBlockParser.java`: comment explaining the profile-vs-family asymmetry.
- `flexmark-core-test/.../ParserEmulationProfileTest.java`: two new tests plus `MutableListOptions` import.

Verification: `mvn -o -q -DskipTests -pl flexmark-core-test -am install` OK;
`ParserEmulationProfileTest` 9 tests, 0 failures; `flexmark-ext-definition` install OK.
