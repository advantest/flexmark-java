/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.plantuml.internal;

import com.google.common.html.HtmlEscapers;
import com.vladsch.flexmark.ext.plantuml.PlantUmlBlockNode;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import net.sourceforge.plantuml.*;
import net.sourceforge.plantuml.preproc.Defines;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlantUmlBlockNodeRenderer implements NodeRenderer {

    @Override
    public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        HashSet<NodeRenderingHandler<?>> set = new HashSet<>();
        set.add(new NodeRenderingHandler<>(PlantUmlBlockNode.class, this::render));
        return set;
    }

    private void render(PlantUmlBlockNode node, NodeRendererContext context, HtmlWriter htmlWriter) {
        renderPlantUmlCode(node.getChars().toString(), htmlWriter, context);
    }

    void renderPlantUmlCode(String plantUmlSourceCode, HtmlWriter htmlWriter, NodeRendererContext context) {
        renderPlantUmlCode(plantUmlSourceCode, null, htmlWriter, context);
    }

    void renderPlantUmlCode(String plantUmlSourceCode, String caption, HtmlWriter htmlWriter, NodeRendererContext context) {
        htmlWriter.tagLine("figure").indent();
        String plantUmlToHtmlResult = translatePlantUmlToHtml(plantUmlSourceCode);
        String htmlFormatted = formatHtml(plantUmlToHtmlResult, context);
        htmlWriter.noTrimLeading().append(htmlFormatted);
        if (caption != null && !caption.isBlank()) {
            String escapedCaption = HtmlEscapers.htmlEscaper().escape(caption);
            htmlWriter.tag("figcaption").append(escapedCaption).tag("/figcaption").line();
        }
        htmlWriter.unIndent().tagLine("/figure");
    }

    void renderErrorMessage(String originalMessage, NodeRendererContext context, HtmlWriter htmlWriter) {
        htmlWriter.withAttr().attr("style", "color:red").tag("span");
        String escapedMessage = HtmlEscapers.htmlEscaper().escape(originalMessage);
        htmlWriter.append(escapedMessage);
        htmlWriter.tag("/span").line();
    }

    private String translatePlantUmlToHtml(String plantUmlSourceCode) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            SourceStringReader reader = new SourceStringReader(plantUmlSourceCode);
            reader.outputImage(os, new FileFormatOption(FileFormat.SVG));
            return new String(os.toByteArray(), Charset.forName("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return "Could not render HTML from PlantUML source code.";
        }
    }

    private String formatHtml(String sourceHtmlCode, NodeRendererContext context) {
        Integer indentSize = HtmlRenderer.INDENT_SIZE.get(context.getOptions());

        if (indentSize == null) {
            indentSize = 2;
        }

        try {
            Source xmlInput = new StreamSource(new StringReader(sourceHtmlCode));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", indentSize.intValue());
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            e.printStackTrace();
            return sourceHtmlCode;
        }
    }

    private String translatePlantUmlToHtml(File plantUmlSourceFile) {
        SourceFileReader reader;
        try {
            File tmpFile = File.createTempFile(plantUmlSourceFile.getName(), "-svg");
            File targetDir = tmpFile.getParentFile();
            reader = new SourceFileReader(Defines.createWithFileName(plantUmlSourceFile),
                    plantUmlSourceFile, targetDir, Collections.<String>emptyList(), "UTF-8", new FileFormatOption(FileFormat.SVG));
            //reader.setCheckMetadata(true);
            List<GeneratedImage> list = reader.getGeneratedImages();

            if (!list.isEmpty()) {
                GeneratedImage img = list.get(0);
                File targetFile = img.getPngFile();

                return Files.readString(targetFile.toPath(), Charset.forName("UTF-8"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Could not render HTML from PlantUML source code.";
    }

    public static class Factory implements NodeRendererFactory {
        @NotNull
        @Override
        public NodeRenderer apply(@NotNull DataHolder options) {
            return new PlantUmlBlockNodeRenderer();
        }
    }

}
