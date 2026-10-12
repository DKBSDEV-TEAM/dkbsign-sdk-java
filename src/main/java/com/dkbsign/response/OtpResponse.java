package com.dkbsign.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OtpResponse {

    private String message;
    private String email;
    private String devOtpCode;

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty("dev_otp_code")
    public String getDevOtpCode() {
        return devOtpCode;
    }

    public void setDevOtpCode(String devOtpCode) {
        this.devOtpCode = devOtpCode;
    }
}
