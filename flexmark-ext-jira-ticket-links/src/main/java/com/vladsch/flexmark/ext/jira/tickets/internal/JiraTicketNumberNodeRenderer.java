/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2026 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.jira.tickets.internal;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.vladsch.flexmark.ext.jira.tickets.JiraTicketNumberNode;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.data.DataHolder;

public class JiraTicketNumberNodeRenderer implements NodeRenderer {

	private final JiraTicketNumbersOptions options;

	public JiraTicketNumberNodeRenderer(DataHolder options) {
		this.options = new JiraTicketNumbersOptions(options);
	}

	@Override
	public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
		Set<NodeRenderingHandler<?>> set = new HashSet<>();
		set.add(new NodeRenderingHandler<>(JiraTicketNumberNode.class, this::render));
		return set;
	}

	private void render(JiraTicketNumberNode node, NodeRendererContext context, HtmlWriter htmlWriter) {
		if (context.isDoNotRenderLinks()) {
			htmlWriter.text(node.getChars());
		} else {
			String targetUrl = options.jiraTicketUrl + node.getChars();

			htmlWriter.srcPos(node.getChars())
				.attr("href", targetUrl)
				.withAttr()
				.tag("a");
			htmlWriter.text(node.getChars());

			htmlWriter.tag("/a");
		}
	}

	public static class Factory implements NodeRendererFactory {
		@NotNull
		@Override
		public NodeRenderer apply(@NotNull DataHolder options) {
			return new JiraTicketNumberNodeRenderer(options);
		}
	}

}
