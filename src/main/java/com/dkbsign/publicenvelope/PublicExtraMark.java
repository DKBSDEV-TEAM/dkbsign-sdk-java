package com.dkbsign.publicenvelope;

import com.dkbsign.enums.SignatureType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Extra mark on a document during public sign ({@code extra_positions}). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class PublicExtraMark {

    private final Integer page;
    private final Double x;
    private final Double y;
    private final Double width;
    private final Double height;
    private final SignatureType type;

    private PublicExtraMark(Integer page, Double x, Double y, Double width, Double height, SignatureType type) {
        this.page = page;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
    }

    public static PublicExtraMark at(int page, double x, double y) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (x < 0 || x > 5000 || y < 0 || y > 5000) {
            throw new IllegalArgumentException("x and y must be between 0 and 5000 mm");
        }
        return new PublicExtraMark(page, x, y, null, null, null);
    }

    public PublicExtraMark size(double width, double height) {
        return new PublicExtraMark(page, x, y, width, height, type);
    }

    public PublicExtraMark type(SignatureType type) {
        return new PublicExtraMark(page, x, y, width, height, type);
    }

    @JsonProperty("page")
    public Integer getPage() {
        return page;
    }

    @JsonProperty("x")
    public Double getX() {
        return x;
    }

    @JsonProperty("y")
    public Double getY() {
        return y;
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
}
