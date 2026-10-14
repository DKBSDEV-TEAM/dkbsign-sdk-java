package com.dkbsign.http;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/** One field of a {@code multipart/form-data} request. */
public final class MultipartPart {

    private final String fieldName;
    private final Path file;
    private final String textContent;

    private MultipartPart(String fieldName, Path file, String textContent) {
        this.fieldName = fieldName;
        this.file = file;
        this.textContent = textContent;
    }

    public static MultipartPart file(String fieldName, Path path) {
        Objects.requireNonNull(path, "path");
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("File not found: " + path);
        }
        return new MultipartPart(fieldName, path, null);
    }

    /** JSON or plain text (e.g. {@code params}, {@code payload}). */
    public static MultipartPart text(String fieldName, String content) {
        if (content == null) {
            throw new IllegalArgumentException("content is required");
        }
        return new MultipartPart(fieldName, null, content);
    }

    public String fieldName() {
        return fieldName;
    }

    public Path file() {
        return file;
    }

    public String textContent() {
        return textContent;
    }

    public boolean isFile() {
        return file != null;
    }
}
