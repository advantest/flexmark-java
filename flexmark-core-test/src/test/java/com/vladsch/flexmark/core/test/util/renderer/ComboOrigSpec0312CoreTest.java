package com.vladsch.flexmark.core.test.util.renderer;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.test.util.TestUtils;
import com.vladsch.flexmark.test.util.spec.ResourceLocation;
import com.vladsch.flexmark.test.util.spec.SpecExample;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jetbrains.annotations.NotNull;
import org.junit.runners.Parameterized;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * One test per example of the unmodified CommonMark 0.31.2 spec. Examples, by their number in the spec,
 * listed in {@value #KNOWN_FAILURES_RESOURCE} are marked with the FAIL option, so they pass while they still render
 * incorrectly and fail as soon as they render correctly, at which point the entry has to be removed.
 * The spec file itself is not modified.
 */
final public class ComboOrigSpec0312CoreTest extends CoreRendererSpecTest {
    final private static String SPEC_RESOURCE = "/spec.0.31.2.txt";
    final private static String KNOWN_FAILURES_RESOURCE = "/spec.0.31.2.known-failures.txt";
    final public static @NotNull ResourceLocation RESOURCE_LOCATION = ResourceLocation.of(SPEC_RESOURCE);

    // DEFAULT configuration, no emulation profile applied. Same settings as OrigSpecCoreTest (INDENT_SIZE overrides the renderer test default), needed to match the original spec expected output
    final private static DataHolder OPTIONS = new MutableDataSet()
            .set(HtmlRenderer.INDENT_SIZE, 0)
            .set(HtmlRenderer.PERCENT_ENCODE_URLS, true)
            .set(TestUtils.NO_FILE_EOL, false)
            .toImmutable();

    final private static Set<Integer> KNOWN_FAILURES = loadKnownFailures();

    public ComboOrigSpec0312CoreTest(@NotNull SpecExample example) {
        super(example, null, OPTIONS);
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> data() {
        // the whole-file entry is covered by FullOrigSpec0312CoreTest, only per-example tests here
        List<Object[]> data = new ArrayList<>();
        int exampleNumber = 0;
        for (Object[] params : getTestData(RESOURCE_LOCATION)) {
            SpecExample example = (SpecExample) params[0];
            if (example.isFullSpecExample()) continue;
            // example numbers of the spec, not the section relative ones used by SpecExample
            exampleNumber++;
            data.add(new Object[] { KNOWN_FAILURES.contains(exampleNumber) ? withFailOption(example) : example });
        }
        assertKnownFailuresExist(exampleNumber);
        return data;
    }

    /** {@link SpecExample#withOptionsSet} replaces the option set, so options declared by the example are kept here. */
    private static @NotNull SpecExample withFailOption(@NotNull SpecExample example) {
        String optionsSet = example.getOptionsSet();
        return example.withOptionsSet(optionsSet == null || optionsSet.trim().isEmpty()
                ? TestUtils.FAIL_OPTION_NAME
                : optionsSet + ", " + TestUtils.FAIL_OPTION_NAME);
    }

    private static void assertKnownFailuresExist(int exampleCount) {
        List<Integer> unknown = new ArrayList<>();
        for (int exampleNumber : KNOWN_FAILURES) {
            if (exampleNumber < 1 || exampleNumber > exampleCount) unknown.add(exampleNumber);
        }
        if (!unknown.isEmpty()) {
            throw new IllegalStateException(KNOWN_FAILURES_RESOURCE + " lists examples which do not exist in "
                    + SPEC_RESOURCE + " (" + exampleCount + " examples): " + unknown);
        }
    }

    private static @NotNull Set<Integer> loadKnownFailures() {
        Set<Integer> result = new HashSet<>();
        try (InputStream stream = ComboOrigSpec0312CoreTest.class.getResourceAsStream(KNOWN_FAILURES_RESOURCE)) {
            if (stream == null) throw new IllegalStateException("Missing resource " + KNOWN_FAILURES_RESOURCE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                int comment = line.indexOf('#');
                String entry = (comment >= 0 ? line.substring(0, comment) : line).trim();
                if (!entry.isEmpty()) result.add(Integer.parseInt(entry));
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }
}