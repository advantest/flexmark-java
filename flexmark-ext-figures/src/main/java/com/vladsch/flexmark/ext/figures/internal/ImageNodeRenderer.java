/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.figures.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.html.HtmlEscapers;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.LinkType;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.html.renderer.ResolvedLink;
import com.vladsch.flexmark.util.ast.TextCollectingVisitor;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.Escaping;

public class ImageNodeRenderer implements NodeRenderer {
	
	@Override
    public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        HashSet<NodeRenderingHandler<?>> set = new HashSet<>();
        set.add(new NodeRenderingHandler<>(Image.class, this::render));
        return set;
    }
	
	private void render(Image node, NodeRendererContext context, HtmlWriter htmlWriter) {
		if (!(node.getParent() instanceof Paragraph)) {
			htmlWriter.tagLine("figure").indent();
		}
		
		// ### Code from com.vladsch.flexmark.html.renderer.CoreNodeRenderer#render(Image, NodeRendererContext, HtmlWriter) ###
		if (context.isDoNotRenderLinks() || isSuppressedLinkPrefix(node.getUrl(), context)) {
			return;
		}
		 
		String altText = new TextCollectingVisitor().collectAndGetText(node);
        ResolvedLink resolvedLink = context.resolveLink(LinkType.IMAGE, node.getUrl().unescape(), null, null);
        String url = resolvedLink.getUrl();

        if (!node.getUrlContent().isEmpty()) {
            // reverse URL encoding of =, &
            String content = Escaping.percentEncodeUrl(node.getUrlContent()).replace("+", "%2B").replace("%3D", "=").replace("%26", "&amp;");
            url += content;
        }
        
        htmlWriter.attr("src", url);
        htmlWriter.attr("alt", altText);

        // we have a title part, use that
        if (node.getTitle().isNotNull()) {
            resolvedLink = resolvedLink.withTitle(node.getTitle().unescape());
        }

        htmlWriter.attr(resolvedLink.getNonNullAttributes());
        htmlWriter.srcPos(node.getChars()).withAttr(resolvedLink).tagVoid("img");
        // ### End of code from com.vladsch.flexmark.html.renderer.CoreNodeRenderer#render(Image, NodeRendererContext, HtmlWriter) ###
        
        if (!(node.getParent() instanceof Paragraph)) {
	        if (altText != null && !altText.isBlank()) {
	            String escapedCaption = HtmlEscapers.htmlEscaper().escape(altText);
	            htmlWriter.line().tag("figcaption").append(escapedCaption).tag("/figcaption").line();
	        }
	        
	        htmlWriter.unIndent().tagLine("/figure");
        }
    }
	
	public static boolean isSuppressedLinkPrefix(CharSequence url, NodeRendererContext context) {
        Pattern suppressedLinks = context.getHtmlOptions().suppressedLinks;
        if (suppressedLinks != null) {
            Matcher matcher = suppressedLinks.matcher(url);
            return matcher.matches();
        }
        return false;
    }
	
	public static class Factory implements NodeRendererFactory {
        @NotNull
        @Override
        public NodeRenderer apply(@NotNull DataHolder options) {
            return new ImageNodeRenderer();
        }
    }

}
