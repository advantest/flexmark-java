/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2026 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.jira.tickets;

import org.jetbrains.annotations.NotNull;

import com.vladsch.flexmark.ext.jira.tickets.internal.JiraTicketNumberInLineParserExtension;
import com.vladsch.flexmark.ext.jira.tickets.internal.JiraTicketNumberNodeRenderer;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.MutableDataHolder;

public class JiraTicketExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {

	public static final DataKey<String> JIRA_URL = new DataKey<>("JIRA_URL", "https://your.atlassian.net/browse/");

	private JiraTicketExtension() {
	}

	public static JiraTicketExtension create() {
		return new JiraTicketExtension();
	}

	@Override
	public void parserOptions(MutableDataHolder options) {

	}

	@Override
	public void rendererOptions(@NotNull MutableDataHolder options) {

	}

	@Override
	public void extend(Parser.Builder parserBuilder) {
		parserBuilder.customInlineParserExtensionFactory(new JiraTicketNumberInLineParserExtension.Factory());
	}

	@Override
	public void extend(@NotNull HtmlRenderer.Builder htmlRendererBuilder, @NotNull String rendererType) {
		if (htmlRendererBuilder.isRendererType("HTML")) {
			htmlRendererBuilder.nodeRendererFactory(new JiraTicketNumberNodeRenderer.Factory());
		}
	}

}
