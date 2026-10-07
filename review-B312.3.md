# Review B312.3 and F3: Unicode symbols are punctuation (CommonMark 0.31.2)

Scope: commits `de7c40c71..35dc088c9` on `common-mark-updates`. Key `Parser.UNICODE_PUNCTUATION_INCLUDES_SYMBOLS`.
Reviewed by a `code-review` sub-agent, one round. It confirmed the rule, the consumers, the gating and all
hand-derived test scenarios; its one finding (a tautological `assertEquals(x, x)` in a smoke test) was fixed.

## What the spec says

`spec.0.30.txt` line 340: a Unicode punctuation character is an ASCII punctuation character or anything in the general
Unicode categories `Pc`, `Pd`, `Pe`, `Pf`, `Pi`, `Po`, or `Ps`.

`spec.0.31.2.txt` line 340: a Unicode punctuation character is a character in the Unicode `P` (punctuation) or `S`
(symbol) general categories.

Changelog `[0.31]`: "Add symbols to unicode punctuation (Titus Wormer)" and "Fix unicode symbols example in
emphasis syntax (rhysd)". commonmark.js uses a regular expression over the same `P` and `S` categories. So the
categories `Sc`, `Sk`, `Sm`, `So` are added; ASCII punctuation was already complete (it includes the ASCII symbols
`$ + < = > ^ ` | ~`).

## The four patterns

Shared parts: `ASCII = '!"#\$%&\*\+,\-\./:;=\?@\\\^_`\|~`, `OPEN = \(<\[\{`, `CLOSE = \)>\]\}`,
`P = \p{Pc}\p{Pd}\p{Pe}\p{Pf}\p{Pi}\p{Po}\p{Ps}`, `PS = P + \p{Sc}\p{Sk}\p{Sm}\p{So}`. Each exists in a 0.30
variant (with `P`, kept for the old profiles) and a 0.31 variant (with `PS`), chosen per `Parsing` instance:

| Field            | Pattern (0.31 variant)                                              |
|------------------|---------------------------------------------------------------------|
| `PUNCTUATION`       | `^[ASCII OPEN CLOSE PS]`                                         |
| `PUNCTUATION_OPEN`  | `^[ASCII OPEN]\|[PS]&&[^CLOSE]`                                  |
| `PUNCTUATION_CLOSE` | `^[ASCII CLOSE]\|[PS]&&[^OPEN]`                                  |
| `PUNCTUATION_ONLY`  | `^[ASCII PS]&&[^OPEN CLOSE]`                                     |

The shapes of the last three are deliberately unchanged, only the category list grew (see next section). Membership
of the set does not depend on `INLINE_DELIMITER_DIRECTIONAL_PUNCTUATIONS`, so all four got the `S` categories.

## Finding: `&&` outside of a character class (pre-existing, NOT fixed, proposed task F5)

Measured with a scratch probe on JDK 21 (deleted), `Matcher.matches()` on every code point as a one-code-point string:

* `PUNCTUATION`: 819 non-ASCII code points (before this change), as expected.
* `PUNCTUATION_OPEN`: matches the ASCII and open sets exactly, **0** non-ASCII code points. It matches
  the 4-character strings `(&&x` and `¡&&x`: the second alternative is the literal `X&&Y`.
* `PUNCTUATION_CLOSE`: the same, 0 non-ASCII code points.
* `PUNCTUATION_ONLY`: matches no single character at all (needs `X&&Y`), and it is not used anywhere.

So in Java regex `&&` is a literal at the top level, as suspected, and the second alternative has no `^` either.
Effect: with `INLINE_DELIMITER_DIRECTIONAL_PUNCTUATIONS` on (default off), no non-ASCII character, not even `¡` or `«`,
is punctuation. The `S` categories in these three patterns are therefore correct in intent and invisible in behaviour.
The tests assert nothing about symbols with the option on, only that letters are letters and nothing throws.

## Cached pattern consumers

`PUNCTUATION*` are plain `Pattern` fields, none of them goes through `Parsing.getCachedPattern`. The only consumer
is `InlineParserImpl.scanDelimiters` (grep over all modules, `*_PUNCTUATION*` and `.PUNCTUATION`). The Parsing
instances created elsewhere (`LightInlineParserImpl`, `JekyllTagBlockParser`, `MacroBlockParser`) get the fields
from the
same constructor. No cache key needed. `HTML_TAG` and the other cached patterns do not use punctuation.

## Behaviour change in extension modules (expected, spec consistent, no test changed)

The full build had no failing extension test. A probe (deleted) compared new and old on extension inputs:

| Input                | Extension     | New                | Old               | Verdict            |
|----------------------|---------------|--------------------|-------------------|--------------------|
| `a~~©x~~b`           | strikethrough | literal            | `<del>`           | same rule as `*`   |
| `H~°~O`              | subscript     | literal            | `<sub>`           | same rule as `*`   |
| `€:+1:`              | emoji         | emoji              | literal           | as after `!`       |
| `"€5"`, `++±x++`, `1.©©` | typographic, ins | same        | same              | unaffected         |

All of these run the same flanking rules as `*`. `EmojiDelimiterProcessor` does `"0123456789".contains(before)`;
`before` is a code point string, so a supplementary character never matches and a symbol is not a digit. Not a
regression. Not covered by tests, the examples were hand-checked against the rules, that is all.

## Tests (`UnicodePunctuationSymbolsTest`, 24 tests, F3)

Derived from the flanking rules, scenarios per symbol (see the class Javadoc): A `a*Sfoo*`, B `*fooS*a`,
C `*S*alpha.` (the shape of example 354), D `_foo_S`, E `S_foo_`. A symbol is punctuation (literal for A to C,
emphasis for D and E), a letter is not (the reverse). Covered: Sc `¥ ¢ £ € ₹`, Sm `± × ÷ ∑`, Sk `¨ ˄ ´ ¯`,
So `© ® ° ♥ ☃`, supplementary Sc U+1E2FF, Sm U+1D6C1, Sk U+1F3FB, So U+1F600, supplementary on both sides of a run
and next to a supplementary letter, strong emphasis, letters (ASCII, BMP, U+1D400) and `Po` as controls, ASCII symbols
(`$ + = ^ | ~`) as always punctuation, the spec 354 shape, an **exhaustive sweep** over every non-ASCII `P` and `S`
code point (over 7,000 symbols and 800 punctuation, on `*` and `_`) against the default and 0.30, the Markdown
round trip through the formatter (no formatter change was needed), gating of the key for every profile and the
behaviour under default, 0.31.2 (new) and 0.26 to 0.30 (old), the directional option smoke tests.

Note: U+1D400 is `Lu`, not a symbol; U+1D6C1 is the supplementary `Sm` used here. `°` is `So`, not `Sk`.

## Expectations changed

None in any existing test. Baseline `spec.0.31.2.known-failures.txt`: entry 354 removed, header only left.

## Not verified

* Only the default and the 0.26 to 0.30 profiles were tested for gating, not `KRAMDOWN`, `MARKDOWN` and the others;
  they are covered by the key test (off for every profile but 0.31.2).
* Unicode version: the sweep follows the JDK 21 tables (Unicode 15). A different JDK would classify new code points
  as they are defined there.
* Extension behaviour was checked by a probe, not by tests.
