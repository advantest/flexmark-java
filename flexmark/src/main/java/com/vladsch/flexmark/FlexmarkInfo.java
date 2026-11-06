package com.vladsch.flexmark;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Information about the flexmark-java library that runs.
 */
public final class FlexmarkInfo {
    final private static String VERSION_RESOURCE = "/META-INF/com.vladsch.flexmark/flexmark/version.properties";
    final private static String VERSION = readVersion(VERSION_RESOURCE);

    private FlexmarkInfo() {
    }

    /**
     * Answers the version of flexmark-java that runs, as the build wrote it into the version resource of the
     * module {@code flexmark}.
     *
     * @return the version, or {@code null} if the version resource cannot be read or holds no version
     */
    public static String getVersion() {
        return VERSION;
    }

    static String readVersion(String resource) {
        try (InputStream stream = FlexmarkInfo.class.getResourceAsStream(resource)) {
            if (stream == null) {
                return null;
            }
            Properties properties = new Properties();
            properties.load(stream);
            String version = properties.getProperty("version");
            return version == null || version.startsWith("${") ? null : version;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }
}
