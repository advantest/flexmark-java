/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2026 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.jira.tickets.internal;

import java.util.Set;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.vladsch.flexmark.ext.jira.tickets.JiraTicketExtension;
import com.vladsch.flexmark.ext.jira.tickets.JiraTicketNumberNode;
import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.InlineParserExtension;
import com.vladsch.flexmark.parser.InlineParserExtensionFactory;
import com.vladsch.flexmark.parser.LightInlineParser;
import com.vladsch.flexmark.util.sequence.BasedSequence;

public class JiraTicketNumberInLineParserExtension implements InlineParserExtension {
	
	private final Pattern REGEX_JIRA_PROJECT_KEY;
	private final Pattern REGEX_JIRA_TICKET_NUMBER;
	
	public JiraTicketNumberInLineParserExtension(LightInlineParser inlineParser) {
		String regex = JiraTicketExtension.JIRA_PROJECT_KEY_REGEX.get(inlineParser.getParsing().options);
		
		REGEX_JIRA_PROJECT_KEY = Pattern.compile(regex);
		REGEX_JIRA_TICKET_NUMBER = Pattern.compile("^" + REGEX_JIRA_PROJECT_KEY + "-\\d+\\b");
	}

	@Override
	public void finalizeDocument(@NotNull InlineParser inlineParser) {
		// nothing to do
	}

	@Override
	public void finalizeBlock(@NotNull InlineParser inlineParser) {
		// nothing to do
	}

	@Override
	public boolean parse(@NotNull LightInlineParser inlineParser) {
		// abort if the preceding character is a word character
		if (inlineParser.getIndex() > 0) {
			char precedingChar = inlineParser.getInput().charAt(inlineParser.getIndex() - 1);
			if (Character.isLetterOrDigit(precedingChar) || precedingChar == '_') {
				return false;
			}
		}
		
		BasedSequence match = inlineParser.match(REGEX_JIRA_TICKET_NUMBER);
		if (match == null) {
			return false;
		}
		
		inlineParser.flushTextNode();
		
		JiraTicketNumberNode jiraTicket = new JiraTicketNumberNode(match);
		inlineParser.getBlock().appendChild(jiraTicket);
		
		return true;
	}
	
	public static class Factory implements InlineParserExtensionFactory {
		@Nullable
		@Override
		public Set<Class<?>> getAfterDependents() {
			return null;
		}

		@NotNull
		@Override
		public CharSequence getCharacters() {
			return "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		}

		@Nullable
		@Override
		public Set<Class<?>> getBeforeDependents() {
			return null;
		}

		@NotNull
		@Override
		public InlineParserExtension apply(@NotNull LightInlineParser inlineParser) {
			return new JiraTicketNumberInLineParserExtension(inlineParser);
		}

		@Override
		public boolean affectsGlobalScope() {
			return false;
		}
	}

}
