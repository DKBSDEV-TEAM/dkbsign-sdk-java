package com.dkbsign.signature;

import com.dkbsign.enums.SignatureLevel;
import com.dkbsign.exception.DkbSignException;
import com.dkbsign.http.ApiResponse;
import com.dkbsign.http.HttpClient;
import com.dkbsign.http.MultipartPart;
import com.dkbsign.model.SelfSignParams;
import com.dkbsign.response.OtpResponse;
import com.dkbsign.response.SecondFactorResponse;
import com.dkbsign.response.SelfSignBatchResponse;
import com.dkbsign.response.SelfSignResponse;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Self-sign: the connected user signs PDFs with their own certificate. */
public final class SelfSignService {

    private static final String OTP_PATH = "/api/v4/sign/otp";
    private static final String SECOND_FACTOR_PATH = "/api/v4/sign/second-factor";
    private static final String SECOND_OTP_PATH = "/api/v4/sign/second-otp";
    private static final String SIGN_PATH = "/api/v4/sign";

    private final HttpClient httpClient;

    public SelfSignService(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    /** Emails an OTP to the connected user, to pass to {@link SelfSignRequest.Builder#otpCode}. */
    public OtpResponse sendOtp() {
        return httpClient.postJson(OTP_PATH, Collections.emptyMap(), OtpResponse.class);
    }


    public SecondFactorResponse checkSecondFactor(SignatureLevel level) {
        Objects.requireNonNull(level, "level");
        return httpClient.getJson(
                SECOND_FACTOR_PATH, Map.of("level", level.getValue()), SecondFactorResponse.class);
    }

    /** Emails a second OTP, for a qualified signature when the user has no TOTP enrolled. */
    public OtpResponse sendSecondOtp() {
        return httpClient.postJson(SECOND_OTP_PATH, Collections.emptyMap(), OtpResponse.class);
    }


    public SelfSignResponse sign(SelfSignRequest request) {
        Objects.requireNonNull(request, "request");
        if (request.files().size() != 1) {
            throw new IllegalArgumentException(
                    "sign() expects one PDF, got " + request.files().size() + "; use signAll()");
        }

        return httpClient.postMultipart(
                SIGN_PATH, selfSignParts(request), SelfSignResponse.class);
    }


    public SelfSignBatchResponse signAll(SelfSignRequest request) {
        Objects.requireNonNull(request, "request");

        ApiResponse response = httpClient.postMultipartRaw(SIGN_PATH, selfSignParts(request));
        return new SelfSignBatchResponse(response.statusCode(), response.body());
    }

    private List<MultipartPart> selfSignParts(SelfSignRequest request) {
        List<MultipartPart> parts = new ArrayList<>();
        String fileField = request.files().size() == 1 ? "file" : "files[]";
        for (Path pdf : request.files()) {
            parts.add(MultipartPart.file(fileField, pdf));
        }
        parts.add(MultipartPart.jsonText("params", paramsJson(request)));
        if (request.signatureImage() != null) {
            parts.add(MultipartPart.file("signature_image", request.signatureImage()));
        }
        return parts;
    }

    private String paramsJson(SelfSignRequest request) {
        String level = request.level() == null ? null : request.level().getValue();
        SelfSignParams params = new SelfSignParams(
                request.pages(),
                level,
                request.otpCode(),
                request.mfaCode(),
                request.secondOtpCode());
        try {
            return httpClient.writeJson(params);
        } catch (IOException e) {
            throw new DkbSignException("Failed to serialize sign parameters", e);
        }
    }
}
