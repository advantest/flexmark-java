/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2026 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.jira.tickets;

import org.jetbrains.annotations.NotNull;

import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;

/**
 * Node representing Jira ticket numbers like HMR-157.
 */
public class JiraTicketNumberNode extends Node {

	protected BasedSequence text = BasedSequence.NULL;

	public JiraTicketNumberNode() {
	}

	public JiraTicketNumberNode(BasedSequence chars) {
		super(chars);
	}

	@Override
	public @NotNull BasedSequence[] getSegments() {
		return EMPTY_SEGMENTS;
	}

}
