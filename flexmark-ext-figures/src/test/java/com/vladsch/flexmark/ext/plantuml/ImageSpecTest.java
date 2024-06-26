/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
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
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ImageSpecTest {
    final private static DataHolder OPTIONS = new MutableDataSet()
            .set(Parser.EXTENSIONS, Collections.singleton(FiguresExtension.create()))
            .set(HtmlRenderer.INDENT_SIZE, 2)
            .toImmutable();

    @Test
    public void referencedPumlFilesRenderedToSvg() throws IOException {
        String expected = readFileFromClasspath("/images.html");
        String mdFileContent = readFileFromClasspath("/images.md");

        Parser parser = Parser.builder(OPTIONS).build();
        HtmlRenderer renderer = HtmlRenderer.builder(OPTIONS).build();
        Document document = parser.parse(mdFileContent);

        String resultHtml = renderer.render(document);

        assertNotNull(resultHtml);
        assertEquals(expected, resultHtml);
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
