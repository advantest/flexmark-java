package com.vladsch.flexmark.ext.abbreviation.internal;

import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.vladsch.flexmark.ext.abbreviation.internal.AbbreviationNodePostProcessor;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;

public class AbbreviationNodePostProcessorTest {
	
	@Test
	public void ensureUnicodeCharactersAreMatched() {
		// see AbbreviationNodePostProcessor.computeAbbreviations(Document)
		String abbreviation = "É.U.";
		String text = "Let's talk about the U.S.A., (" + abbreviation + ")...";
		String pattern = "\\b\\Q" + abbreviation + "\\E";
		
		// Without  Pattern.UNICODE_CHARACTER_CLASS flag, the abbreviation is not matched in Java 21,
		// but it does match in Java 11 and 17.
		Pattern compiledPattern = Pattern.compile(pattern, Pattern.UNICODE_CHARACTER_CLASS);
		
		Matcher matcher = compiledPattern.matcher(text);
		boolean found = false;
		while(matcher.find()) {
			String matched = matcher.group();
			
			if (abbreviation.equals(matched)) {
				found = true;
			}
		}
		
		assertTrue(found);
	}
	
}
