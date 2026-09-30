package com.vladsch.flexmark.core.test.util.renderer;

import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.test.util.spec.ResourceLocation;
import com.vladsch.flexmark.util.data.DataHolder;
import org.jetbrains.annotations.NotNull;

/**
 * The default specification resource spec.txt is the specification of the version COMMONMARK_LATEST
 * points at, so it is tested with that version's profile.
 * <p>
 * Currently this duplicates {@link FullOrigSpec030CoreTest}, because COMMONMARK_LATEST is COMMONMARK_0_30 and
 * spec.txt is the 0.30 spec. Its purpose is to catch COMMONMARK_LATEST and spec.txt drifting apart.
 */
final public class FullOrigSpecCoreTest extends OrigSpecCoreTest {
    static final String SPEC_RESOURCE = "/spec.txt";
    final public static @NotNull ResourceLocation RESOURCE_LOCATION = ResourceLocation.of(SPEC_RESOURCE);
    final private static DataHolder OPTIONS = ParserEmulationProfile.COMMONMARK_LATEST.getProfileOptions().toImmutable();

    public FullOrigSpecCoreTest() {
        super(OPTIONS);
    }

    @Override
    @NotNull
    protected ResourceLocation getSpecResourceLocation() {
        return RESOURCE_LOCATION;
    }
}
