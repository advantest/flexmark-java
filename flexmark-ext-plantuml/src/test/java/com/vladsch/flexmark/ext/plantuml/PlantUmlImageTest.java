/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2022-2026 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.plantuml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.vladsch.flexmark.util.ast.Document;

public class PlantUmlImageTest extends AbstractPlantUmlTest {

	private static final String REGEX_IMAGES_RENDERED = """
			<h1>Heading</h1>
			<p>Some text
			here and
			there\\.</p>
			<p><img src=\\"path/to/file\\.png\\" alt=\\"Some image\\" /></p>
			<p>Some more text with <img src=\\"folder/file\\.jpg\\" alt=\\"icon\\" /> and image in-lined\\.</p>
			<figure>
			  <svg [^<>]+>[\\s\\S]+
			  </svg>
			  <figcaption>PlantUML diagram</figcaption>
			</figure>
			<p>Follow-up text\\.</p>
			""";

	@Test
	public void referencedPumlFilesRenderedToSvg() throws IOException {
		URL resource = this.getClass().getResource("/images.md");
		String mdFileContent = readFileFromClasspath("/images.md");

		Document document = parser.parse(mdFileContent);
		document.set(PlantUmlExtension.KEY_DOCUMENT_FILE_PATH, resource.getPath());
		Map<String, String> referencedFileContents = new HashMap<>();
		referencedFileContents.put("diagrams/classes.puml", readFileFromClasspath("/diagrams/classes.puml"));
		document.set(PlantUmlExtension.KEY_DOCUMENT_PATH_TO_FILE_CONTENTS_MAP, referencedFileContents);

		String resultHtml = renderer.render(document);

		assertNotNull(resultHtml);
		assertTrue(resultHtml.matches(REGEX_IMAGES_RENDERED));
	}

	@Test
	public void renderErrorMessageForMissingPumlFile() {
		URL resource = this.getClass().getResource("/images.md");
		String mdFileContent = "![label](path/to/missing/file.puml)";

		Document document = parser.parse(mdFileContent);
		document.set(PlantUmlExtension.KEY_DOCUMENT_FILE_PATH, resource.getPath());

		String resultHtml = renderer.render(document);

		assertNotNull(resultHtml);
		String expectedPrefix = "<span style=\"color:red\">PlantUML file";
		String expectedSuffix = "does not exist.</span>\n";
		assertEquals(expectedPrefix, resultHtml.substring(0, expectedPrefix.length()));
		assertEquals(expectedSuffix, resultHtml.substring(resultHtml.length() - expectedSuffix.length()));
	}

}
