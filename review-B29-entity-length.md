# Review B29 entity length: numeric character reference limits of CommonMark 0.29

Scope: commits `b1e540461^..HEAD` on `common-mark-updates`. Numeric character references are limited to 7 decimal
and 6 hex digits. Reviewed by a `code-review` sub-agent (one round).

## Findings

| # | Finding                                                                                             | Outcome  |
|---|-----------------------------------------------------------------------------------------------------|----------|
| 1 | Limits match the 0.29 spec wording; IDI and NO_IDI variants are consistent; no other entity pattern | Confirmed |
| 2 | Four more `HtmlEntity` consumers still used `unescape()` (Jira, YouTrack, DOCX, enum. reference)    | Applied  |
| 3 | Legacy profiles lose 8 digit decoding in link destinations and titles (`Escaping` is static)        | Declined |
| 4 | "All nine 0.29 keys off" proxy for the legacy limit is fragile for partially configured options     | Declined |

## Applied

* The four consumers decode `HtmlEntity` nodes with `Html5Entities.entityToString`, like the core renderer.
* Tests: a partially configured 0.28 profile gets the 0.29 limit, and `TextCollectingVisitor` decodes consistently.
* VERSION.md names the selection rule and the link destination behaviour.

## Declined

* A legacy variant of `Escaping` (finding 3). It has no access to options, and none of the 0.26 to 0.28 spec
  examples has an 8 digit reference outside of text. Link destinations follow the 0.29 limit, documented in VERSION.md.
* A dedicated option instead of the proxy (finding 4). The user decided against a new `DataKey` and against changes
  to `ParserEmulationProfile`; the coupling is documented and pinned by a test.
