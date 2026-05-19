package com.vladsch.flexmark.test.util.spec;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Used to resolve test resource URL from copy in test location to URL of source file
 */
public interface ResourceUrlResolver extends Function<String, String> {
    @Override
    String apply(String externalForm);

    String FILE_PROTOCOL_SINGLE_SLASH = "file:/";
    String FILE_PROTOCOL_DOUBLE_SLASH = FILE_PROTOCOL_SINGLE_SLASH + "/";

    static boolean isFileProtocol(@NotNull String externalForm) {
        return externalForm.startsWith(FILE_PROTOCOL_SINGLE_SLASH);
    }

    static boolean hasProtocol(@NotNull String externalForm) {
        int pos = externalForm.indexOf(":");
        // allow windows drive letter to not be treated as protocol
        return pos > 1;
    }

    @NotNull
    static String removeProtocol(@NotNull String externalForm) {
        int pos = externalForm.indexOf(':');
        if (pos > 0) {
            return externalForm.substring(pos + 1);
        }
        return externalForm;
    }
}
