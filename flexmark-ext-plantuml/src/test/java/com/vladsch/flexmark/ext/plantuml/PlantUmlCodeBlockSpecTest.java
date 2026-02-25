/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.plantuml;

import com.vladsch.flexmark.core.test.util.RendererSpecTest;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.test.util.spec.ResourceLocation;
import com.vladsch.flexmark.test.util.spec.SpecExample;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.runners.Parameterized;

import java.util.Collections;
import java.util.List;

// Ignore this test, since its spec is highly dependent on the PlantUML and Graphviz versions.
// In addition, the flexmark implementation is not capable of loading files correctly on Windows.
@Ignore
public class PlantUmlCodeBlockSpecTest extends RendererSpecTest {
    private static final String SPEC_RESOURCE = "/ext_plantuml_ast_spec.md";
    
    @NotNull
    public static final ResourceLocation RESOURCE_LOCATION = ResourceLocation.of(SPEC_RESOURCE);
    
    private static final DataHolder OPTIONS = new MutableDataSet()
            .set(Parser.EXTENSIONS, Collections.singleton(PlantUmlExtension.create()))
            .set(PlantUmlExtension.KEY_RENDER_FENCED_PLANTUML_CODE_BLOCKS, true)
            .toImmutable();

    public PlantUmlCodeBlockSpecTest(@NotNull SpecExample example) {
        super(example, Collections.emptyMap(), OPTIONS);
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> data() {
        return getTestData(RESOURCE_LOCATION);
    }
}
