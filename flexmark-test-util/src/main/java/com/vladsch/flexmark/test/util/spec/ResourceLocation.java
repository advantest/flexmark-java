package com.vladsch.flexmark.test.util.spec;

import com.vladsch.flexmark.test.util.ComboSpecTestCase;
import com.vladsch.flexmark.test.util.TestUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ResourceLocation {
    final public static ResourceLocation NULL = of(Object.class, "", "");

    final private @NotNull Class<?> resourceClass;
    final private @NotNull String resourcePath;
    final private @NotNull String fileUrl;

    public ResourceLocation(@NotNull Class<?> resourceClass, @NotNull String resourcePath, @NotNull String fileUrl) {
        this.resourceClass = resourceClass;
        this.resourcePath = resourcePath;
        this.fileUrl = fileUrl;
    }

    @NotNull
    public Class<?> getResourceClass() {
        return resourceClass;
    }

    @NotNull
    public String getResourcePath() {
        return resourcePath;
    }

    @NotNull
    public String getFileUrl() {
        return fileUrl;
    }

    @NotNull
    public String getFileDirectoryUrl() {
        int pos = fileUrl.lastIndexOf(File.separatorChar);
        if (pos > 0) {
            return fileUrl.substring(0, pos + 1);
        }
        return fileUrl;
    }

    @NotNull
    public String getFileUrl(int lineNumber) {
        return TestUtils.getUrlWithLineNumber(getFileUrl(), lineNumber);
    }

    public boolean isNull() {
        return this == NULL;
    }

    @NotNull
    public InputStream getResourceInputStream() {
        return getResourceInputStream(this);
    }

    @NotNull
    public String getResourceText() {
        return getResourceText(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceLocation location = (ResourceLocation) o;

        if (!resourceClass.equals(location.resourceClass)) return false;
        if (!resourcePath.equals(location.resourcePath)) return false;
        return fileUrl.equals(location.fileUrl);
    }

    // @formatter:off
    @NotNull public ResourceLocation withResourceClass(@NotNull Class<?> resourceClass) { return new ResourceLocation(resourceClass, resourcePath,  fileUrl); };
    @NotNull public ResourceLocation withResourcePath(@NotNull String resourcePath) { return new ResourceLocation(resourceClass, resourcePath,  fileUrl); };
    @NotNull public ResourceLocation withFileUrl(@NotNull String fileUrl) { return new ResourceLocation(resourceClass, resourcePath,  fileUrl); };
    @NotNull public ResourceLocation withResolvedResourcePath(@NotNull String resolvedResourcePath) { return new ResourceLocation(resourceClass, resourcePath,  fileUrl); };
    // @formatter:on

    @Override
    public int hashCode() {
        int result = resourceClass.hashCode();
        result = 31 * result + resourcePath.hashCode();
        result = 31 * result + fileUrl.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ResourceLocation {" +
                " resourceClass=" + resourceClass +
                ", resourcePath='" + resourcePath + '\'' +
                '}';
    }

    public static @NotNull ResourceLocation of(@NotNull String resourcePath) {
        return new ResourceLocation(ComboSpecTestCase.class, resourcePath,
                TestUtils.getSpecResourceFileUrl(ComboSpecTestCase.class, resourcePath)
        );
    }

    public static @NotNull ResourceLocation of(@NotNull Class<?> resourceClass, @NotNull String resourcePath) {
        return new ResourceLocation(resourceClass, resourcePath,
                TestUtils.getSpecResourceFileUrl(resourceClass, resourcePath)
        );
    }

    public static @NotNull ResourceLocation of(@NotNull Class<?> resourceClass, @NotNull String resourcePath, @NotNull String fileUrl) {
        return new ResourceLocation(resourceClass, resourcePath, fileUrl);
    }

    @NotNull
    public static String getResourceText(@NotNull ResourceLocation location) {
        StringBuilder sb = new StringBuilder();
        try {
            String line;
            InputStream inputStream = getResourceInputStream(location);
            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader);
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            reader.close();
            streamReader.close();
            inputStream.close();
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static InputStream getResourceInputStream(@NotNull ResourceLocation location) {
        InputStream stream = location.getResourceClass().getResourceAsStream(location.getResourcePath());
        if (stream == null) {
            throw new IllegalStateException("Could not load " + location);
        }

        return stream;
    }
}
