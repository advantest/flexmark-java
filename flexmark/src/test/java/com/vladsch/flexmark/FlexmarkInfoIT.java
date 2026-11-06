package com.vladsch.flexmark;

import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Runs against the packaged JAR, as failsafe puts the JAR rather than the class folder on the class path.
 */
public class FlexmarkInfoIT {
    final private static String VERSION_RESOURCE = "/META-INF/com.vladsch.flexmark/flexmark/version.properties";

    private static String expectedVersion() {
        String expected = System.getProperty("expectedVersion");
        assertNotNull("system property expectedVersion is not set", expected);
        return expected;
    }

    @Test
    public void test_versionResource_comesFromJar() {
        URL resource = FlexmarkInfo.class.getResource(VERSION_RESOURCE);
        assertNotNull(resource);
        assertEquals(resource.toString(), "jar", resource.getProtocol());
    }

    @Test
    public void test_getVersion_answersProjectVersion() {
        assertEquals(expectedVersion(), FlexmarkInfo.getVersion());
    }

    @Test
    public void test_manifest_hasImplementationVersion() throws Exception {
        URL location = FlexmarkInfo.class.getProtectionDomain().getCodeSource().getLocation();
        try (JarFile jar = new JarFile(new File(location.toURI()))) {
            assertNotNull("JAR has no manifest", jar.getManifest());
            assertEquals(expectedVersion(), jar.getManifest().getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION));
        }
    }
}
