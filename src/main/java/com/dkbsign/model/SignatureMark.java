package com.dkbsign.model;

import com.dkbsign.enums.SignatureType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;


@JsonInclude(JsonInclude.Include.NON_NULL)
public final class SignatureMark {

    private final Double x;
    private final Double y;
    private final String anchor;
    private final Integer page;
    private final Integer occurrence;
    private final Double width;
    private final Double height;
    private final SignatureType type;

    private SignatureMark(
            Double x,
            Double y,
            String anchor,
            Integer page,
            Integer occurrence,
            Double width,
            Double height,
            SignatureType type) {
        this.x = x;
        this.y = y;
        this.anchor = anchor;
        this.page = page;
        this.occurrence = occurrence;
        this.width = width;
        this.height = height;
        this.type = type;
    }

    /** Position of the mark, in millimetres. */
    public static SignatureMark at(double x, double y) {
        requireNonNegative(x, "x");
        requireNonNegative(y, "y");
        return new SignatureMark(x, y, null, null, null, null, null, null);
    }

    public static SignatureMark anchor(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("anchor text is required");
        }
        return new SignatureMark(null, null, text, null, null, null, null, null);
    }

    /** Box size, in points. */
    public SignatureMark size(double width, double height) {
        requirePositive(width, "width");
        requirePositive(height, "height");
        return new SignatureMark(x, y, anchor, page, occurrence, width, height, type);
    }

    public SignatureMark type(SignatureType type) {
        Objects.requireNonNull(type, "type");
        return new SignatureMark(x, y, anchor, page, occurrence, width, height, type);
    }


    /** 0-based page: required for coordinate marks in envelopes; optional anchor search limit for self-sign. */
    public SignatureMark onPage(int page) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        return new SignatureMark(x, y, anchor, page, occurrence, width, height, type);
    }


    public SignatureMark occurrence(int occurrence) {
        requireAnchor("occurrence");
        if (occurrence < 0) {
            throw new IllegalArgumentException("occurrence must be >= 0");
        }
        return new SignatureMark(x, y, anchor, page, occurrence, width, height, type);
    }

    @JsonProperty("x")
    public Double getX() {
        return x;
    }

    @JsonProperty("y")
    public Double getY() {
        return y;
    }

    @JsonProperty("anchor")
    public String getAnchor() {
        return anchor;
    }

    @JsonProperty("page")
    public Integer getPage() {
        return page;
    }

    @JsonProperty("occurrence")
    public Integer getOccurrence() {
        return occurrence;
    }

    @JsonProperty("width")
    public Double getWidth() {
        return width;
    }

    @JsonProperty("height")
    public Double getHeight() {
        return height;
    }

    @JsonProperty("type")
    public SignatureType getType() {
        return type;
    }

    private void requireAnchor(String method) {
        if (anchor == null) {
            throw new IllegalStateException(method + "() only applies to anchor marks");
        }
    }

    private static void requireNonNegative(double value, String name) {
        if (!(value >= 0)) {
            throw new IllegalArgumentException(name + " must be >= 0");
        }
    }

    private static void requirePositive(double value, String name) {
        if (!(value > 0)) {
            throw new IllegalArgumentException(name + " must be > 0");
        }
    }
}
