package com.dkbsign.publicenvelope;

import com.dkbsign.DkbSignClient;
import com.dkbsign.http.HttpClient;
import com.dkbsign.http.MultipartPart;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Public signer session ({@code /api/v4/public/envelopes/{signer_uuid}/...}). No Bearer token.
 * Open the link received by the signer ({@code signing_url}) and pass its signer UUID here.
 */
public final class PublicEnvelopeService {

    private final HttpClient httpClient;
    private final String signerUuid;

    private PublicEnvelopeService(String baseUrl, String signerUuid) {
        this.httpClient = new HttpClient(baseUrl, null);
        this.signerUuid = requireUuid(signerUuid);
    }

    public static PublicEnvelopeService create(String baseUrl, String signerUuid) {
        return new PublicEnvelopeService(baseUrl, signerUuid);
    }

    public static PublicEnvelopeService create(String signerUuid) {
        return create(DkbSignClient.DEFAULT_BASE_URL, signerUuid);
    }

    public String getSignerUuid() {
        return signerUuid;
    }

    public PublicSigningSession getSession() {
        return httpClient.getPublicJson(basePath(), null, PublicSigningSession.class);
    }


    public void requestOtp() {
        httpClient.postPublicJson(basePath() + "/otp", Collections.emptyMap(), Void.class);
    }


    public void requestSecondOtp() {
        httpClient.postPublicJson(basePath() + "/second-otp", Collections.emptyMap(), Void.class);
    }

    public PublicOtpVerifyResponse verifyOtp(String otpCode) {
        if (otpCode == null || otpCode.isBlank()) {
            throw new IllegalArgumentException("otpCode is required");
        }
        return httpClient.postPublicJson(
                basePath() + "/verify",
                Map.of("otp_code", otpCode.trim()),
                PublicOtpVerifyResponse.class);
    }

    public PublicIdentityResponse enrollIdentity(PublicIdentityEnrollRequest request) {
        Objects.requireNonNull(request, "request");
        List<MultipartPart> parts = new ArrayList<>();
        parts.add(MultipartPart.file("document", request.document()));
        parts.add(MultipartPart.formField("document_type", request.documentType().getValue()));
        for (Path frame : request.selfieFrames()) {
            parts.add(MultipartPart.file("selfie_frames[]", frame));
        }
        parts.add(MultipartPart.formField("consent", "true"));
        if (request.documentNumberLast4() != null && !request.documentNumberLast4().isBlank()) {
            parts.add(MultipartPart.formField("document_number_last4", request.documentNumberLast4().trim()));
        }
        return httpClient.postPublicMultipart(basePath() + "/identity/enroll", parts, PublicIdentityResponse.class);
    }

    public PublicIdentityResponse rescanIdentity(List<Path> selfieFrames) {
        if (selfieFrames == null || selfieFrames.size() < 3 || selfieFrames.size() > 8) {
            throw new IllegalArgumentException("Between 3 and 8 selfie frames are required");
        }
        List<MultipartPart> parts = new ArrayList<>();
        for (Path frame : selfieFrames) {
            parts.add(MultipartPart.file("selfie_frames[]", frame));
        }
        return httpClient.postPublicMultipart(basePath() + "/identity/rescan", parts, PublicIdentityResponse.class);
    }

    public PublicIdentityResponse documentCheckIdentity(PublicIdentityEnrollRequest request) {
        Objects.requireNonNull(request, "request");
        List<MultipartPart> parts = new ArrayList<>();
        parts.add(MultipartPart.file("document", request.document()));
        parts.add(MultipartPart.formField("document_type", request.documentType().getValue()));
        for (Path frame : request.selfieFrames()) {
            parts.add(MultipartPart.file("selfie_frames[]", frame));
        }
        return httpClient.postPublicMultipart(
                basePath() + "/identity/document-check", parts, PublicIdentityResponse.class);
    }


    public void prepareIdentity() {
        httpClient.postPublicJson(basePath() + "/identity/prepare", Collections.emptyMap(), Void.class);
    }

    public PublicSignResponse sign() {
        return sign(PublicSignRequest.builder().build());
    }

    public PublicSignResponse sign(PublicSignRequest request) {
        Objects.requireNonNull(request, "request");
        return httpClient.postPublicJson(basePath() + "/sign", request, PublicSignResponse.class);
    }

    public void refuse() {
        refuse(null, null);
    }

    public void refuse(String reason, String reasonCode) {
        if (reason == null && reasonCode == null) {
            httpClient.postPublicJson(basePath() + "/refuse", Collections.emptyMap(), Void.class);
            return;
        }
        java.util.Map<String, String> body = new java.util.LinkedHashMap<>();
        if (reason != null && !reason.isBlank()) {
            body.put("reason", reason.trim());
        }
        if (reasonCode != null && !reasonCode.isBlank()) {
            body.put("reason_code", reasonCode.trim());
        }
        httpClient.postPublicJson(basePath() + "/refuse", body, Void.class);
    }

    public byte[] previewDocument(String documentUuid) {
        Objects.requireNonNull(documentUuid, "documentUuid");
        return httpClient.getPublicBytes(
                "/api/v4/public/documents/" + documentUuid.trim() + "/preview",
                Map.of("signer", signerUuid));
    }

    public void previewDocument(String documentUuid, Path target) {
        Objects.requireNonNull(target, "target");
        try {
            java.nio.file.Files.write(target, previewDocument(documentUuid));
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException("Failed to write " + target, e);
        }
    }

    private String basePath() {
        return "/api/v4/public/envelopes/" + signerUuid;
    }

    private static String requireUuid(String signerUuid) {
        if (signerUuid == null || signerUuid.isBlank()) {
            throw new IllegalArgumentException("signerUuid is required");
        }
        return signerUuid.trim();
    }
}
