/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2026 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.jira.tickets.internal;

import org.jetbrains.annotations.NotNull;

import com.vladsch.flexmark.ext.jira.tickets.JiraTicketExtension;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSetter;

public class JiraTicketNumbersOptions implements MutableDataSetter {
	
	public final String jiraTicketUrl;

	public JiraTicketNumbersOptions(DataHolder options) {
		jiraTicketUrl = JiraTicketExtension.JIRA_URL.get(options);
	}

	@NotNull
	@Override
	public MutableDataHolder setIn(@NotNull MutableDataHolder dataHolder) {
		dataHolder.set(JiraTicketExtension.JIRA_URL, jiraTicketUrl);
		return dataHolder;
	}

}
