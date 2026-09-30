package com.vladsch.flexmark.core.test.util.renderer;

import com.vladsch.flexmark.test.util.spec.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Runs the unmodified spec.0.29.txt with the DEFAULT configuration, no emulation profile is applied, so it proves
 * that a plain Parser.builder() and HtmlRenderer.builder() are CommonMark 0.29 compliant out of the box.
 * <p>
 * The only options set are the ones of {@link OrigSpecCoreTest}, which are test harness concerns rather than
 * parsing behaviour: HtmlRenderer.PERCENT_ENCODE_URLS (the spec expects percent encoded URLs) and
 * TestUtils.NO_FILE_EOL (the spec examples have no trailing file EOL handling). No profile or parser option is set.
 */
final public class FullSpec029DefaultOptionsCoreTest extends OrigSpecCoreTest {
    static final String SPEC_RESOURCE = "/spec.0.29.txt";
    final public static @NotNull ResourceLocation RESOURCE_LOCATION = ResourceLocation.of(SPEC_RESOURCE);

    public FullSpec029DefaultOptionsCoreTest() {
        super(null);
    }

    @Override
    @NotNull
    protected ResourceLocation getSpecResourceLocation() {
        return RESOURCE_LOCATION;
    }
}