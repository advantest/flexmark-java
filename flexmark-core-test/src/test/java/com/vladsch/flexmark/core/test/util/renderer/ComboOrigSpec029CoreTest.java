package com.vladsch.flexmark.core.test.util.renderer;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.test.util.TestUtils;
import com.vladsch.flexmark.test.util.spec.ResourceLocation;
import com.vladsch.flexmark.test.util.spec.SpecExample;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jetbrains.annotations.NotNull;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;

/**
 * One test per example of the unmodified CommonMark 0.29 spec. The spec file itself is not modified.
 */
final public class ComboOrigSpec029CoreTest extends CoreRendererSpecTest {
    final private static String SPEC_RESOURCE = "/spec.0.29.txt";
    final public static @NotNull ResourceLocation RESOURCE_LOCATION = ResourceLocation.of(SPEC_RESOURCE);

    // same settings as OrigSpecCoreTest (INDENT_SIZE overrides the renderer test default), needed to match the original spec expected output
    final private static DataHolder OPTIONS = new MutableDataSet()
            .setFrom(ParserEmulationProfile.COMMONMARK_0_29.getProfileOptions())
            .set(HtmlRenderer.INDENT_SIZE, 0)
            .set(HtmlRenderer.PERCENT_ENCODE_URLS, true)
            .set(TestUtils.NO_FILE_EOL, false)
            .toImmutable();

    public ComboOrigSpec029CoreTest(@NotNull SpecExample example) {
        super(example, null, OPTIONS);
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> data() {
        // the whole-file entry is covered by FullOrigSpec029CoreTest, only per-example tests here
        List<Object[]> data = new ArrayList<>();
        for (Object[] params : getTestData(RESOURCE_LOCATION)) {
            SpecExample example = (SpecExample) params[0];
            if (example.isFullSpecExample()) continue;
            data.add(new Object[] { example });
        }
        return data;
    }
}
