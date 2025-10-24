/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.plantuml.internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.html.HtmlEscapers;
import com.vladsch.flexmark.ext.plantuml.PlantUmlBlockNode;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.SequenceUtils;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.GeneratedImage;
import net.sourceforge.plantuml.SourceFileReader;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.preproc.Defines;

public class PlantUmlBlockNodeRenderer implements NodeRenderer {
	
	private static final String REGEX_SVG_TAG = "<svg\\s.*?>";
	private static final String REGEX_ATTR_VALUE = "(?<attribute>\\S+)=\\\"(?<value>.*?)\\\"";
	private static final Pattern SVG_TAG_PATTERN = Pattern.compile(REGEX_SVG_TAG, Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
	private static final Pattern ATTRIBUTE_VALUE_PATTERN = Pattern.compile(REGEX_ATTR_VALUE);

    @Override
    public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        HashSet<NodeRenderingHandler<?>> set = new HashSet<>();
        set.add(new NodeRenderingHandler<>(PlantUmlBlockNode.class, this::render));
        return set;
    }

    private void render(PlantUmlBlockNode node, NodeRendererContext context, HtmlWriter htmlWriter) {
        renderPlantUmlCode(node.getChars().toString(), htmlWriter, context);
    }

    public void renderPlantUmlCode(String plantUmlSourceCode, HtmlWriter htmlWriter, NodeRendererContext context) {
        renderPlantUmlCode(plantUmlSourceCode, null, htmlWriter, context);
    }

    public void renderPlantUmlCode(String plantUmlSourceCode, String caption, HtmlWriter htmlWriter, NodeRendererContext context) {
        htmlWriter.tagLine("figure").indent();
        String plantUmlToSvgResult = translatePlantUmlToSvg(plantUmlSourceCode);
        String plantUmlToHtmlResult = adaptSvgAttributesForHtmlEmbedding(plantUmlToSvgResult);
        String htmlFormatted = formatHtml(plantUmlToHtmlResult, context);
        htmlWriter.noTrimLeading().append(htmlFormatted);
        if (caption != null && !caption.isBlank()) {
            String escapedCaption = HtmlEscapers.htmlEscaper().escape(caption);
            htmlWriter.line().tag("figcaption").append(escapedCaption).tag("/figcaption").line();
        }
        htmlWriter.unIndent().tagLine("/figure");
    }

    void renderErrorMessage(String originalMessage, NodeRendererContext context, HtmlWriter htmlWriter) {
        htmlWriter.withAttr().attr("style", "color:red").tag("span");
        String escapedMessage = HtmlEscapers.htmlEscaper().escape(originalMessage);
        htmlWriter.append(escapedMessage);
        htmlWriter.tag("/span").line();
    }
    
    private String adaptSvgAttributesForHtmlEmbedding(String svgCode) {
    	List<MatchResult> matches = SVG_TAG_PATTERN.matcher(svgCode).results().toList();
		
    	if (matches.isEmpty()) {
    		return svgCode;
    	}
    	
    	StringBuilder svgCodeBuilder = new StringBuilder();
    	int cursorIndex = 0;
    	for (MatchResult match : matches) {
    		// append everything between last match (or first code char) and this match
    		svgCodeBuilder.append(svgCode.substring(cursorIndex, match.start()));
    		
    		String svgTag = match.group();
    		String attributes = svgTag.substring("<svg".length(), svgTag.length() - 1);
    		
    		// append "<svg "
    		svgCodeBuilder.append(svgTag.substring(0, "<svg".length()));
    		svgCodeBuilder.append(' ');
    		
    		// collect all attributes and their values
    		Map<String, String> svgAttributeValuePairs = new LinkedHashMap<>();
    		ATTRIBUTE_VALUE_PATTERN.matcher(attributes).results()
    			.forEach(m -> svgAttributeValuePairs.put(m.group("attribute"), m.group("value")));
    		
    		// we don't want height attribute, we only need the width attribute
    		svgAttributeValuePairs.remove("height");
    		
    		// we don't want preserveAspectRatio="none"
    		svgAttributeValuePairs.remove("preserveAspectRatio");
    		
    		// we need to modify the entries of style attribute
    		String styleValue = svgAttributeValuePairs.get("style");
    		if (styleValue != null && !styleValue.isBlank()) {
    			Map<String,String> styleAttrValuePairs = new LinkedHashMap<>();
    			Arrays.stream(styleValue.split(";\s*"))
    					.forEach(attrValuePair -> {
    						String[] parts = attrValuePair.split("\s*:\s*");
    						if (parts.length == 2) {
    							styleAttrValuePairs.put(parts[0], parts[1]);
    						}
    					});
    			
    			// remove width and height attributes from style entry
    			styleAttrValuePairs.remove("width");
    			styleAttrValuePairs.remove("height");
    			
    			// add max-width attribute
    			styleAttrValuePairs.put("max-width", "100%");
    			
    			StringBuilder styleAttrBuilder = new StringBuilder();
    			for (String styleAttrName : styleAttrValuePairs.keySet()) {
    				styleAttrBuilder.append(styleAttrName);
    				styleAttrBuilder.append(": ");
    				styleAttrBuilder.append(styleAttrValuePairs.get(styleAttrName));
    				styleAttrBuilder.append("; ");
    			}
    			styleValue = styleAttrBuilder.toString().trim();
    			svgAttributeValuePairs.put("style", styleValue);
    		}
    		
    		int index = 0;
    		int numKeys = svgAttributeValuePairs.keySet().size();
    		for (String svgAttrName : svgAttributeValuePairs.keySet()) {
    			svgCodeBuilder.append(svgAttrName);
    			svgCodeBuilder.append('=');
    			svgCodeBuilder.append('"');
    			svgCodeBuilder.append(svgAttributeValuePairs.get(svgAttrName));
    			svgCodeBuilder.append('"');
    			index++;
    			
    			if (index != numKeys) {
    				svgCodeBuilder.append(' ');
    			}
    		}
    		
    		svgCodeBuilder.append(">");
    		
    		cursorIndex = match.end();
    	}
    	
    	// append everything after last match
    	svgCodeBuilder.append(svgCode.substring(cursorIndex));
    	
    	return svgCodeBuilder.toString();
    }

    private String unifyLineEndings(String lines) {
    	if (lines == null) {
    		return null;
    	}
    	
    	String[] linesArray = lines.split("\\r\\n|\\n");
    	return String.join(SequenceUtils.EOL, linesArray);
    }

    private String translatePlantUmlToSvg(String plantUmlSourceCode) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            SourceStringReader reader = new SourceStringReader(plantUmlSourceCode);
            reader.outputImage(os, new FileFormatOption(FileFormat.SVG));
            return new String(os.toByteArray(), Charset.forName("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return "Could not render SVG from PlantUML source code.";
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
            String formatted = xmlOutput.getWriter().toString();
            return unifyLineEndings(formatted);
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
