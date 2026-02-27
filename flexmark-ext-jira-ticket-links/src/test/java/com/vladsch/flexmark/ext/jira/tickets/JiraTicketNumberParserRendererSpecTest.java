/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.jira.tickets;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.junit.runners.Parameterized;

import com.vladsch.flexmark.core.test.util.RendererSpecTest;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.test.util.spec.ResourceLocation;
import com.vladsch.flexmark.test.util.spec.SpecExample;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class JiraTicketNumberParserRendererSpecTest extends RendererSpecTest {

	private static final String SPEC_RESOURCE = "/ext_jira_ast_spec.md";
	
	@NotNull
	public static final ResourceLocation RESOURCE_LOCATION = ResourceLocation.of(SPEC_RESOURCE);
	
	private static final DataHolder OPTIONS = new MutableDataSet()
			.set(Parser.EXTENSIONS, Collections.singleton(JiraTicketExtension.create()))
			.toImmutable();
	
	private static final Map<String, DataHolder> optionsMap = new HashMap<>();
	static {
		optionsMap.put("custom_root", new MutableDataSet().set(JiraTicketExtension.JIRA_URL, "https://www.your-domain.com/browse/"));
		optionsMap.put("custom_proj_key_regex", new MutableDataSet().set(JiraTicketExtension.JIRA_PROJECT_KEY_REGEX, "[A-Z0-9][A-Z_0-9]+"));
	}

	public JiraTicketNumberParserRendererSpecTest(@NotNull SpecExample example) {
		super(example, optionsMap, OPTIONS);
	}

	@Parameterized.Parameters(name = "{0}")
	public static List<Object[]> data() {
		return getTestData(RESOURCE_LOCATION);
	}
}
