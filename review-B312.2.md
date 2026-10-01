# Review B312.2: HTML block type 6 tag names of CommonMark 0.31.2

Scope: commits `88f3d294c..1eba2e497` on `common-mark-updates`. Key `Parser.HTML_BLOCK_TAGS_SEARCH_NOT_SOURCE`.
Reviewed by a `code-review` sub-agent, one round, no findings. The cluster is latent: no spec example covers it, the
known-failures baseline is byte identical, the hand-written tests are the only evidence.

## What the spec says

Type 6 start condition, `spec.0.30.txt` against `spec.0.31.2.txt`: only the tag list differs.

* removed: `source` (0.30 line `` `section`, `source`, `summary`, ``)
* added: `search` (0.31.2 line `` `search`, `section`, `summary`, ``)

Changelog `[0.31]`: "Add `search` element to list of known block elements (Titus Wormer)" and "Remove `source`
element as HTML block start condition (Lukas Spieß)".

## Tag list copies found

| Copy                                                  | Used when                            | Fixed |
|-------------------------------------------------------|--------------------------------------|-------|
| `Parser.HTML_BLOCK_TAGS`                              | always (`HtmlBlockParser` type 6)    | yes   |
| `HtmlDeepParser.BLOCK_TAGS`                           | `HTML_BLOCK_DEEP_PARSER`             | yes   |
| `FlexmarkHtmlConverter` (html2md, own list)           | HTML to Markdown, not the parser     | no, different purpose |
| spec and test resources (`spec*.txt`, `ast_spec.md`)  | documentation and tests              | no    |

`HtmlDeepParser.VOID_TAGS` also lists `source`. That is correct HTML (`source` is a void element) and unrelated to
the block start rule.

## Decisions worth knowing

* **User supplied `HTML_BLOCK_TAGS`.** The flag only selects the *default value* of `HTML_BLOCK_TAGS`: the key now has
  a factory default that picks the 0.31.2 or the 0.30 list by the flag. The list is never post-processed, so a user
  who sets `HTML_BLOCK_TAGS` gets exactly that list under every profile and flag value. Adding or removing a tag from
  a user list behind their back would be the surprising alternative. `HTML_BLOCK_TAGS.get(null)` returns the 0.31.2
  list, the same as the default of every other newest-behaviour key.
* **Deep parser.** `HtmlDeepParser` always unions its built-in set with `HTML_BLOCK_TAGS`, so with the default key
  `source` would have come back through the union. It now takes the flag and swaps `search`/`source` in its built-in
  set. A user list is still unioned, as before, so a user list cannot remove a built-in tag in deep mode (older
  behaviour, unchanged). The old constructor `HtmlDeepParser(List)` stays and means 0.31.2.
* The default lists are unmodifiable (`Collections.unmodifiableList`); callers copy them (`new ArrayList<>(...)`).

## Behaviour confirmed by the tests (`HtmlBlockTagsSearchNotSourceTest`, 19 tests)

* `<search>` and `</search>` start a raw block and interrupt a paragraph; `<source>` does neither and its content is
  inline HTML with emphasis. Default, `COMMONMARK_0_31_2`, with and without the deep parser.
* 0.26 to 0.30 keep `source` as a block and `search` as inline HTML.
* Explicit `HTML_BLOCK_TAGS` wins; the flag alone switches the default; configurations do not leak into each other.
* Markdown to Markdown: all blocks and inlines survive the formatter, no formatter change was needed.
* A complete tag alone on a line (`<source>` followed by text on the next line) is still an HTML block of type 7,
  as the spec says; only the shared-line and paragraph-interrupt cases changed.

## Not verified / known quirks left alone

* `math` is still in `HTML_BLOCK_TAGS`. No CommonMark version lists it; origin unknown, out of scope.
* In deep parser mode a void tag (`source`, `hr`, `link`, ...) never interrupts a paragraph, because the deep parser
  does not mark void tags as first block tag. Older quirk, present with the 0.30 list too, so the deep parser test for
  `source` interrupting a paragraph under old profiles is restricted to the non-deep parser.
* `GITHUB_DOC` and `MULTI_MARKDOWN` profiles were not tested for the tag list (different paragraph interruption and
  soft break rules); they are covered only by the profile flag test.
* The formatter inserts a blank line between a paragraph and a following HTML block (older behaviour); the tests
  normalise that.