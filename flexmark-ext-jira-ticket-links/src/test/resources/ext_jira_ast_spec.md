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


