package com.vladsch.flexmark.test.util.spec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.jetbrains.annotations.NotNull;

import com.vladsch.flexmark.test.util.ComboSpecTestCase;
import com.vladsch.flexmark.test.util.TestUtils;

public class ResourceLocation {
    final public static ResourceLocation NULL = of(Object.class, "", emptyUrl());
    
    private static URL emptyUrl() {
        try {
            return URI.create("file:/").toURL();
        } catch (MalformedURLException e) {
        }
        return null;
    }

    final private @NotNull Class<?> resourceClass;
    final private @NotNull String resourcePath;
    final private @NotNull URL fileUrl;
    final private @NotNull String resolvedResourcePath;

    public ResourceLocation(@NotNull Class<?> resourceClass, @NotNull String resourcePath, @NotNull URL fileUrl) {
        this(resourceClass, resourcePath, fileUrl, TestUtils.getResolvedSpecResourcePath(resourceClass, resourcePath));
    }
    
    public ResourceLocation(@NotNull Class<?> resourceClass, @NotNull String resourcePath, @NotNull String fileUrl) {
        this(resourceClass, resourcePath, fromUrlString(fileUrl));
    }
    
    private static URL fromUrlString(String fileUrl) {
    	try {
			return URI.create(fileUrl).toURL();
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
    }

    private ResourceLocation(@NotNull Class<?> resourceClass, @NotNull String resourcePath, @NotNull URL fileUrl, @NotNull String resolvedResourcePath) {
        this.resourceClass = resourceClass;
        this.resourcePath = resourcePath;
        this.fileUrl = fileUrl;
        this.resolvedResourcePath = resolvedResourcePath;
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
    public URL getFileUrl() {
        return fileUrl;
    }

    @NotNull
    public String getFileDirectoryUrl() {
    	String path = fileUrl.getPath();
        int pos = path.lastIndexOf('/');
        if (pos > 0) {
            return path.substring(0, pos + 1);
        }
        return path;
    }

    @NotNull
    public String getFileUrl(int lineNumber) {
        return TestUtils.getUrlWithLineNumber(getFileUrl().toString(), lineNumber);
    }

    @NotNull
    public String getResolvedResourcePath() {
        return resolvedResourcePath;
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
        if (!fileUrl.equals(location.fileUrl)) return false;
        return resolvedResourcePath.equals(location.resolvedResourcePath);
    }

    // @formatter:off
    @NotNull public ResourceLocation withResourceClass(@NotNull Class<?> resourceClass) { return new ResourceLocation(resourceClass, resourcePath,  fileUrl, resolvedResourcePath); };
    @NotNull public ResourceLocation withResourcePath(@NotNull String resourcePath) { return new ResourceLocation(resourceClass, resourcePath,  fileUrl, resolvedResourcePath); };
    @NotNull public ResourceLocation withFileUrl(@NotNull URL fileUrl) { return new ResourceLocation(resourceClass, resourcePath,  fileUrl, resolvedResourcePath); };
    @NotNull public ResourceLocation withResolvedResourcePath(@NotNull String resolvedResourcePath) { return new ResourceLocation(resourceClass, resourcePath,  fileUrl, resolvedResourcePath); };
    // @formatter:on

    @Override
    public int hashCode() {
        int result = resourceClass.hashCode();
        result = 31 * result + resourcePath.hashCode();
        result = 31 * result + fileUrl.hashCode();
        result = 31 * result + resolvedResourcePath.hashCode();
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
                TestUtils.getSpecResourceFileUrl(ComboSpecTestCase.class, resourcePath),
                TestUtils.getResolvedSpecResourcePath(ComboSpecTestCase.class, resourcePath)
        );
    }

    public static @NotNull ResourceLocation of(@NotNull Class<?> resourceClass, @NotNull String resourcePath) {
        return new ResourceLocation(resourceClass, resourcePath,
                TestUtils.getSpecResourceFileUrl(resourceClass, resourcePath),
                TestUtils.getResolvedSpecResourcePath(resourceClass, resourcePath)
        );
    }

    public static @NotNull ResourceLocation of(@NotNull Class<?> resourceClass, @NotNull String resourcePath, @NotNull URL fileUrl) {
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
        try {
            return location.getFileUrl().openStream();
        } catch (IOException e) {
            throw new IllegalStateException("Could not load " + location.getFileUrl().toString());
        }
    }
}
