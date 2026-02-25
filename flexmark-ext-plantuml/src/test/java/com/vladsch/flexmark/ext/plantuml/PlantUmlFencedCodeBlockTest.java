/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2026 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.plantuml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.vladsch.flexmark.util.ast.Document;

public class PlantUmlFencedCodeBlockTest extends AbstractPlantUmlTest {

	@Test
	public void simpleFencedPlantUmlCodeBlockRendered() {
		final String markdownCode = """
				```plantuml
				@startuml
				  interface SomeInterface
				  SomeInterface <|.. SomeClass
				@enduml
				```
				""";
		final String expectedRegex = """
				<figure>
				  <svg [^<>]+>[\\s\\S]+
				  </svg>
				</figure>
				""";
		
		Document document = parser.parse(markdownCode);
		String resultHtml = renderer.render(document);
		
		assertNotNull(resultHtml);
		assertTrue(resultHtml.matches(expectedRegex));
	}
	
	@Test
	public void simpleFencedPlantUmlCodeBlockWithEmptyLineFollowingRendered() {
		final String markdownCode = """
				```plantuml
				@startuml
				  interface SomeInterface
				  SomeInterface <|.. SomeClass
				@enduml
				```
				
				""";
		final String expectedRegex = """
				<figure>
				  <svg [^<>]+>[\\s\\S]+
				  </svg>
				</figure>
				""";
		
		Document document = parser.parse(markdownCode);
		String resultHtml = renderer.render(document);
		
		assertNotNull(resultHtml);
		assertTrue(resultHtml.matches(expectedRegex));
	}
	
	@Test
	public void simpleFencedPlantUmlCodeBlockAlternativeSyntaxRendered() {
		final String markdownCode = """
				~~~plantuml
				@startuml
				  interface SomeInterface
				  SomeInterface <|.. SomeClass
				@enduml
				~~~
				""";
		final String expectedRegex = """
				<figure>
				  <svg [^<>]+>[\\s\\S]+
				  </svg>
				</figure>
				""";
		
		Document document = parser.parse(markdownCode);
		String resultHtml = renderer.render(document);
		
		assertNotNull(resultHtml);
		assertTrue(resultHtml.matches(expectedRegex));
	}
	
	@Test
	public void fencedNonPlantUmlCodeBlockRendered() {
		final String markdownCode = """
				```
				@startuml
				  interface SomeInterface
				  SomeInterface <|.. SomeClass
				@enduml
				```
				""";
		final String expected = """
				<pre><code>@startuml
				  interface SomeInterface
				  SomeInterface &lt;|.. SomeClass
				@enduml
				</code></pre>
				""";
		
		Document document = parser.parse(markdownCode);
		String resultHtml = renderer.render(document);
		
		assertNotNull(resultHtml);
		assertEquals(resultHtml, expected);
	}
	
	@Test
	public void fencedNonPlantUmlCodeBlockWithLanguageRendered() {
		final String markdownCode = """
				```plantumlcode
				@startuml
				  interface SomeInterface
				  SomeInterface <|.. SomeClass
				@enduml
				```
				""";
		final String expected = """
				<pre><code class="language-plantumlcode">@startuml
				  interface SomeInterface
				  SomeInterface &lt;|.. SomeClass
				@enduml
				</code></pre>
				""";
		
		Document document = parser.parse(markdownCode);
		String resultHtml = renderer.render(document);
		
		assertNotNull(resultHtml);
		assertEquals(resultHtml, expected);
	}
}
