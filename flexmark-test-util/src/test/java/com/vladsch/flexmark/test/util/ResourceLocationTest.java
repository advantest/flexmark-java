/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import com.vladsch.flexmark.test.util.spec.ResourceLocation;

public class ResourceLocationTest {
	
	@Test
	public void test_resolvingRelativePath() throws Exception {
		final String resourcePath = "/test_spec.md";
		
		ResourceLocation location = ResourceLocation.of(resourcePath);
		
		assertNotNull(location);
		assertEquals(resourcePath, location.getResourcePath());
		
		assertNotNull(location.getFileUrl());
		assertEquals("file", location.getFileUrl().getProtocol());
		assertTrue(location.getFileUrl().getPath().endsWith(resourcePath));
		
		File file = new File(location.getResolvedResourcePath());
		assertTrue(file.exists());
		assertTrue(file.isFile());
		
		assertNotNull(location.getResourceInputStream());
	}

}
