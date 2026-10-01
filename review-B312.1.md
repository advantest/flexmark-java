# Review B312.1: inline HTML comment grammar of CommonMark 0.31.2

Scope: commits `19ca738f6..HEAD` on `common-mark-updates`. Key `Parser.HTML_COMMENT_ANY_TEXT`, profile
`COMMONMARK_0_31_2`, formatter fix for `<!-->` and `<!--->`. Reviewed by a `code-review` sub-agent, one round,
no genuine findings.

## Findings

| # | Round | Finding                                                                                      | Outcome   |
|---|-------|----------------------------------------------------------------------------------------------|-----------|
| 1 | 1     | Regex equals the spec grammar, possessive loop, no catastrophic backtracking                 | Confirmed |
| 2 | 1     | `HTML_TAG` cache key has the `_COMMENT_0312` discriminator, it is the only user of the rule  | Confirmed |
| 3 | 1     | Every `COMMONMARK_0_30` branch handles `COMMONMARK_0_31_2`, 0.26 to 0.30 keep the old rule   | Confirmed |
| 4 | 1     | Formatter guard covers inline, block and inner block comments                                | Confirmed |
| 5 | 1     | `HtmlBlockParser` inner block comment splitting looks for `-->` from offset 4                | Declined  |

## Declined

* #5: for `<!--> x -->` inside an HTML block the inner comment node spans to the later `-->` instead of ending at
  `<!-->`. Only the AST segmentation of this rare input is affected, the rendered HTML and the block end are
  correct. Outside the scope of the example-driven cluster, recorded here.

## Decisions worth knowing

* Regex: `<!-->|<!--->|<!--[^-]*+(?:-(?!->)[^-]*+)*+-->`, instead of `(?:(?!-->)[\s\S])*`, because the loop then
  iterates only per hyphen and long comments cannot overflow the stack (tested with 20000 repetitions).
* The default-options spec tests for 0.29 and 0.30 had to set `HTML_COMMENT_ANY_TEXT=false`: the default is now the
  0.31.2 comment rule, so they can no longer pass with an untouched default configuration.
* Not verified: the formatter still adds a blank line after a trailing comment in a list item and drops it between a
  block comment and a paragraph; both are older behaviour unrelated to the grammar and were not touched.