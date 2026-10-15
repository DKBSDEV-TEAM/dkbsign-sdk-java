package com.dkbsign.http;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;


public final class MultipartPart {

    public enum Kind {
        FILE,

        JSON_TEXT,

        FORM_FIELD
    }

    private final String fieldName;
    private final Path file;
    private final String textContent;
    private final Kind kind;

    private MultipartPart(String fieldName, Path file, String textContent, Kind kind) {
        this.fieldName = fieldName;
        this.file = file;
        this.textContent = textContent;
        this.kind = kind;
    }

    public static MultipartPart file(String fieldName, Path path) {
        Objects.requireNonNull(path, "path");
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("File not found: " + path);
        }
        return new MultipartPart(fieldName, path, null, Kind.FILE);
    }


    public static MultipartPart jsonText(String fieldName, String content) {
        if (content == null) {
            throw new IllegalArgumentException("content is required");
        }
        return new MultipartPart(fieldName, null, content, Kind.JSON_TEXT);
    }


    public static MultipartPart formField(String fieldName, String value) {
        if (value == null) {
            throw new IllegalArgumentException("value is required");
        }
        return new MultipartPart(fieldName, null, value, Kind.FORM_FIELD);
    }


    @Deprecated
    public static MultipartPart text(String fieldName, String content) {
        return jsonText(fieldName, content);
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

    public Kind kind() {
        return kind;
    }

    public boolean isFile() {
        return kind == Kind.FILE;
    }
}
