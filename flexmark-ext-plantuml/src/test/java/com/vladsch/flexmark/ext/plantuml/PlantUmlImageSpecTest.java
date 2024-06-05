/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.plantuml;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PlantUmlImageSpecTest {
    final private static DataHolder OPTIONS = new MutableDataSet()
            .set(Parser.EXTENSIONS, Collections.singleton(PlantUmlExtension.create()))
            .set(HtmlRenderer.INDENT_SIZE, 2)
            .toImmutable();

    @Test
    public void referencedPumlFilesRenderedToSvg() throws IOException {
        String expected = readFileFromClasspath("/images.html");
        URL resource = this.getClass().getResource("/images.md");
        String mdFileContent = readFileFromClasspath("/images.md");

        Parser parser = Parser.builder(OPTIONS).build();
        HtmlRenderer renderer = HtmlRenderer.builder(OPTIONS).build();
        Document document = parser.parse(mdFileContent);
        document.set(PlantUmlExtension.KEY_DOCUMENT_FILE_PATH, resource.getPath());
        Map<String,String> referencedFileContents = new HashMap<>();
        referencedFileContents.put("diagrams/classes.puml", readFileFromClasspath("/diagrams/classes.puml"));
        document.set(PlantUmlExtension.KEY_DOCUMENT_PATH_TO_FILE_CONTENTS_MAP, referencedFileContents);

        String resultHtml = renderer.render(document);

        assertNotNull(resultHtml);
        assertEquals(expected, resultHtml);
    }

    @Test
    public void renderErrorMessageForMissingPumlFile() throws IOException {
        URL resource = this.getClass().getResource("/images.md");
        String mdFileContent = "![label](path/to/missing/file.puml)";

        Parser parser = Parser.builder(OPTIONS).build();
        HtmlRenderer renderer = HtmlRenderer.builder(OPTIONS).build();
        Document document = parser.parse(mdFileContent);
        document.set(PlantUmlExtension.KEY_DOCUMENT_FILE_PATH, resource.getPath());

        String resultHtml = renderer.render(document);

        assertNotNull(resultHtml);
        String expectedPrefix = "<span style=\"color:red\">PlantUML file";
        String expectedSuffix = "does not exist.</span>\n";
        assertEquals(expectedPrefix, resultHtml.substring(0, expectedPrefix.length()));
        assertEquals(expectedSuffix, resultHtml.substring(resultHtml.length() - expectedSuffix.length()));
    }

    private String readFileFromClasspath(String filePath) throws IOException {
        StringBuffer contents = new StringBuffer();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                this.getClass().getResourceAsStream(filePath)))) {
        	String line = reader.readLine();
        	while (line != null) {
        		contents.append(line);
                contents.append("\n");
                line = reader.readLine();
        	}
        }
        return contents.toString();
    }
}
