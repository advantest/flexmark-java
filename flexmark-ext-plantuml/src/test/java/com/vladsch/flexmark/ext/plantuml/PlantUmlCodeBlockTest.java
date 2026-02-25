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

public class PlantUmlCodeBlockTest extends AbstractPlantUmlTest {

	@Test
	public void simplePlantUmlCodeBlockRendered() {
		final String markdownCode = """
				@startuml
				    class ArrayList
				    interface List
				    
				    ArrayList ..|> List
				@enduml
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
	public void simplePlantUmlCodeBlockWithEmptyLineFollowingRendered() {
		final String markdownCode = """
				@startuml
				    class ArrayList
				    interface List
				    
				    ArrayList ..|> List
				@enduml
				
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
	public void invalidPlantUmlCodeBlockInHtml() {
		final String markdownCode = """
				@startuml
				    class ArrayList
				    interface List
				    
				    ArrayList ..|> List
				@endsomething
				""";
		final String expected = """
				<p>@startuml
				class ArrayList
				interface List</p>
				<pre><code>ArrayList ..|&gt; List
				</code></pre>
				<p>@endsomething</p>
				""";
		
		Document document = parser.parse(markdownCode);
		String resultHtml = renderer.render(document);
		
		assertNotNull(resultHtml);
		assertEquals(resultHtml, expected);
	}
	
	@Test
	public void simplePlantUmlCodeBlockWithContextRendered() {
		final String markdownCode = """
				# Heading
				
				Some text
				in a paragraph.
				
				@startuml
				    class ArrayList
				    interface List
				    
				    ArrayList ..|> List
				@enduml
				
				Another paragraph.
				""";
		final String expectedRegex = """
				<h1>Heading</h1>
				<p>Some text
				in a paragraph\\.</p>
				<figure>
				  <svg [^<>]+>[\\s\\S]+
				  </svg>
				</figure>
				<p>Another paragraph\\.</p>
				""";
		
		Document document = parser.parse(markdownCode);
		String resultHtml = renderer.render(document);
		
		assertNotNull(resultHtml);
		assertTrue(resultHtml.matches(expectedRegex));
	}
	
	@Test
	public void timingDiagramRendered() {
		final String markdownCode = """
				# Timing diagram example
				
				@startuml
				robust "DNS Resolver" as DNS
				robust "Web Browser" as WB
				concise "Web User" as WU
				
				@0
				WU is Idle
				WB is Idle
				DNS is Idle
				
				@+100
				WU -> WB : URL
				WU is Waiting
				WB is Processing
				
				@+200
				WB is Waiting
				WB -> DNS@+50 : Resolve URL
				
				@+100
				DNS is Processing
				
				@+300
				DNS is Idle
				@enduml
				
				A paragraph following.
				""";
		final String expectedRegex = """
				<h1>Timing diagram example</h1>
				<figure>
				  <svg [^<>]+>[\\s\\S]+
				  </svg>
				</figure>
				<p>A paragraph following.</p>
				""";
		
		Document document = parser.parse(markdownCode);
		String resultHtml = renderer.render(document);
		
		assertNotNull(resultHtml);
		assertTrue(resultHtml.matches(expectedRegex));
	}
}
