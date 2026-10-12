package com.dkbsign.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignPage {

    private final Integer page;
    private final List<SignatureMark> signatures;

    /** {@code page} is null for anchor marks, which the API searches across the document. */
    public SignPage(Integer page, List<SignatureMark> signatures) {
        this.page = page;
        this.signatures = List.copyOf(signatures);
    }

    @JsonProperty("page")
    public Integer getPage() {
        return page;
    }

    @JsonProperty("signatures")
    public List<SignatureMark> getSignatures() {
        return signatures;
    }
}
