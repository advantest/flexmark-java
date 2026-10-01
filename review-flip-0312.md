# Review: defaults flip to CommonMark 0.31.2

Scope: commits 248ffb2bd..HEAD on common-mark-updates. Reviewed by a code-review sub-agent, one round. It found
the gating of all 16 keys and the suite registration correct. Three findings, all fixed: stale "0.30 is the default"
comments in four tests, a missing blank line in VERSION.md that merged two paragraphs, and the then-missing
eview-flip-0312.md (this file).

## Change

* COMMONMARK_LATEST is COMMONMARK_0_31_2 (Javadoc names 0.31.2 and COMMONMARK_0_30 as the pin).
* Gating audit of ParserEmulationProfile.setIn and getOptions: the nine 0.29 keys (and the list option in
  getOptions) are off for every profile except 0.29, 0.30 and 0.31.2. The four 0.30 keys are off for every profile
  except 0.30 and 0.31.2. The three 0.31.2 keys are off for every profile except 0.31.2. Layering is exhaustive,
  no code change needed.
* spec.txt is a byte-exact copy of spec.0.31.2.txt, SHA-256 `257C41AD946F7A1414A499ACA402A1AA8FDAC3678532266611348C1CF54F4B80` for both.
* Added FullOrigSpec0312CoreTest and FullSpec0312DefaultOptionsCoreTest (super(null), the only default-options
  full-spec test). Deleted FullSpec029DefaultOptionsCoreTest and FullSpec030DefaultOptionsCoreTest, which had to
  pin HTML_COMMENT_ANY_TEXT=false. Deleted ComboOrigSpec0312CoreTest and spec.0.31.2.known-failures.txt.
* ParserEmulationProfileTest follows LATEST and spec.txt to 0.31.2. FullOrigSpecCoreTest uses
  COMMONMARK_LATEST against spec.txt, so it now runs 0.31.2 with COMMONMARK_0_31_2 (coherent, passes).

## Real signal: SpecIntegrationTest

Advancing spec.txt broke SpecIntegrationTest (4 failures). It overrides three autolink examples by source text,
and 0.31.2 changed those examples from http:// to https://. The keys and expected URLs were updated. Rendering
behaviour did not change, only the example text did.

## Docs

Changed: VERSION.md, README.md (CommonMark spec 0.29 to 0.31.2, added 0.30 and 0.31.2 links),
comply-with-commonmark-plan.md (Status, Task B312 heading, coverage table). Left: README.md history entry for
0.28, other *.md files naming old versions (historic or analysis documents), follow-ups F1, F5, F6, F7.

Not verified: link label case folding stays a Java approximation (unchanged); no external 0.31.2 reference
implementation comparison beyond the spec examples.

