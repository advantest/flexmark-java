package com.vladsch.flexmark.core.test.util.renderer;

import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.test.util.spec.ResourceLocation;
import com.vladsch.flexmark.util.data.DataHolder;
import org.jetbrains.annotations.NotNull;

final public class FullOrigSpec029CoreTest extends OrigSpecCoreTest {
    static final String SPEC_RESOURCE = "/spec.0.29.txt";
    final public static @NotNull ResourceLocation RESOURCE_LOCATION = ResourceLocation.of(SPEC_RESOURCE);
    final private static DataHolder OPTIONS = ParserEmulationProfile.COMMONMARK_0_29.getProfileOptions().toImmutable();

    public FullOrigSpec029CoreTest() {
        super(OPTIONS);
    }
    
    @Override
    @NotNull
    protected ResourceLocation getSpecResourceLocation() {
        // FIX: implement 0.29 spec and enable test. Final zero-failures gate: enable only when 0.29 is fully
        // implemented. Until then per-example coverage is ComboOrigSpec029CoreTest with the known failures
        // baseline in spec.0.29.known-failures.txt.
        //return RESOURCE_LOCATION;
        return ResourceLocation.NULL;
    }
}
