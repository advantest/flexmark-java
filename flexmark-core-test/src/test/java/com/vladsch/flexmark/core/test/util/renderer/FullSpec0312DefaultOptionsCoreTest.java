package com.vladsch.flexmark.core.test.util.renderer;

import com.vladsch.flexmark.test.util.spec.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Runs the unmodified spec.0.31.2.txt with the DEFAULT configuration, no emulation profile and no parser option is
 * applied, so it proves that a plain Parser.builder() and HtmlRenderer.builder() are CommonMark 0.31.2 compliant
 * out of the box. This is the single test asserting that the out-of-the-box configuration is compliant, it tracks
 * the newest supported specification version and moves on whenever the default is advanced.
 * <p>
 * Older versions are asserted through their explicit profiles: FullOrigSpec026CoreTest to FullOrigSpec030CoreTest
 * each prove that the profile of that version reproduces the specification of that version.
 * <p>
 * The only options set are the ones of {@link OrigSpecCoreTest}, which are test harness concerns rather than
 * parsing behaviour: HtmlRenderer.PERCENT_ENCODE_URLS (the spec expects percent encoded URLs) and
 * TestUtils.NO_FILE_EOL (the spec examples have no trailing file EOL handling).
 */
final public class FullSpec0312DefaultOptionsCoreTest extends OrigSpecCoreTest {
    static final String SPEC_RESOURCE = "/spec.0.31.2.txt";
    final public static @NotNull ResourceLocation RESOURCE_LOCATION = ResourceLocation.of(SPEC_RESOURCE);

    public FullSpec0312DefaultOptionsCoreTest() {
        super(null);
    }

    @Override
    @NotNull
    protected ResourceLocation getSpecResourceLocation() {
        return RESOURCE_LOCATION;
    }
}