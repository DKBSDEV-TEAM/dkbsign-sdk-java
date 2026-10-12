package com.dkbsign.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SelfSignParams {

    private final List<SignPage> pages;
    private final String signatureLevel;
    private final String otpCode;
    private final String mfaCode;
    private final String secondOtpCode;

    public SelfSignParams(
            List<SignPage> pages,
            String signatureLevel,
            String otpCode,
            String mfaCode,
            String secondOtpCode) {
        this.pages = List.copyOf(pages);
        this.signatureLevel = signatureLevel;
        this.otpCode = otpCode;
        this.mfaCode = mfaCode;
        this.secondOtpCode = secondOtpCode;
    }

    @JsonProperty("pages")
    public List<SignPage> getPages() {
        return pages;
    }

    @JsonProperty("signature_level")
    public String getSignatureLevel() {
        return signatureLevel;
    }

    @JsonProperty("otp_code")
    public String getOtpCode() {
        return otpCode;
    }

    @JsonProperty("mfa_code")
    public String getMfaCode() {
        return mfaCode;
    }

    @JsonProperty("second_otp_code")
    public String getSecondOtpCode() {
        return secondOtpCode;
    }
}
