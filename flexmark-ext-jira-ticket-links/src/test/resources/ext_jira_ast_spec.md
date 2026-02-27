---
title: Jira Ticket Numbers Extension Spec
author: Dietrich Travkin
version:
date: '2026-02-25'
license: '[CC-BY-SA 4.0](http://creativecommons.org/licenses/by-sa/4.0/)'
...

---

# Jira Ticket Numbers

Converts Jira ticket numbers like `HMR-157` to an HTML hyperlink to the Jira ticket.

flexmark-java extension for math formula support in Markdown code.

---

## In-line ticket

Single ticket, in-line

```````````````````````````````` example In-line ticket: 1
Some text HMR-157 followed by more text.
.
<p>Some text <a href="https://your.atlassian.net/browse/HMR-157">HMR-157</a> followed by more text.</p>
.
Document[0, 40]
  Paragraph[0, 40]
    Text[0, 10] chars:[0, 10, "Some text "]
    JiraTicketNumberNode[10, 17]
    Text[17, 40] chars:[17, 40, " foll … text."]
````````````````````````````````

URL customizable

```````````````````````````````` example(In-line ticket: 2) options(custom_root)
Some text HMR-157 followed by more text.
.
<p>Some text <a href="https://www.your-domain.com/browse/HMR-157">HMR-157</a> followed by more text.</p>
.
Document[0, 40]
  Paragraph[0, 40]
    Text[0, 10] chars:[0, 10, "Some text "]
    JiraTicketNumberNode[10, 17]
    Text[17, 40] chars:[17, 40, " foll … text."]
````````````````````````````````

Project key regex customizable (to some extend)

```````````````````````````````` example(In-line ticket: 3) options(custom_proj_key_regex)
Some text 4ME-17 followed by more text.
.
<p>Some text <a href="https://your.atlassian.net/browse/4ME-17">4ME-17</a> followed by more text.</p>
.
Document[0, 39]
  Paragraph[0, 39]
    Text[0, 10] chars:[0, 10, "Some text "]
    JiraTicketNumberNode[10, 16]
    Text[16, 39] chars:[16, 39, " foll … text."]
````````````````````````````````


```````````````````````````````` example(In-line ticket: 4) options(custom_root,custom_proj_key_regex)
Some text 4ME-17 followed by more text.
.
<p>Some text <a href="https://www.your-domain.com/browse/4ME-17">4ME-17</a> followed by more text.</p>
.
Document[0, 39]
  Paragraph[0, 39]
    Text[0, 10] chars:[0, 10, "Some text "]
    JiraTicketNumberNode[10, 16]
    Text[16, 39] chars:[16, 39, " foll … text."]
````````````````````````````````

---

## Non-tickets

Ticket numbers have to begin and end at a word boundary

```````````````````````````````` example Non-tickets: 1
textHMR-157
.
<p>textHMR-157</p>
.
Document[0, 11]
  Paragraph[0, 11]
    Text[0, 11] chars:[0, 11, "textH … R-157"]
````````````````````````````````

```````````````````````````````` example Non-tickets: 2
HMR-157text
.
<p>HMR-157text</p>
.
Document[0, 11]
  Paragraph[0, 11]
    Text[0, 11] chars:[0, 11, "HMR-1 … 7text"]
````````````````````````````````

Tickets have to begin with a big letter

```````````````````````````````` example Non-tickets: 3
45PROJ-46 _PRO-5 proj-346
.
<p>45PROJ-46 _PRO-5 proj-346</p>
.
Document[0, 25]
  Paragraph[0, 25]
    Text[0, 25] chars:[0, 25, "45PRO … j-346"]
````````````````````````````````

Tickets need at least two characters in the project key and at least one digit in its number

```````````````````````````````` example Non-tickets: 4
P-173 PROJECT- -8764 
.
<p>P-173 PROJECT- -8764</p>
.
Document[0, 21]
  Paragraph[0, 21]
    Text[0, 20] chars:[0, 20, "P-173 … -8764"]
````````````````````````````````