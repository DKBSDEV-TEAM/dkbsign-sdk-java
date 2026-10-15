package com.dkbsign.envelope;

import com.dkbsign.exception.DkbSignException;
import com.dkbsign.http.HttpClient;
import com.dkbsign.http.MultipartPart;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Create and manage signing envelopes ({@code /api/v4/envelopes/...}). */
public final class EnvelopeService {

    private static final String ENVELOPES_PATH = "/api/v4/envelopes";

    private final HttpClient httpClient;

    public EnvelopeService(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    public CreateEnvelopeResponse create(CreateEnvelopeRequest request) {
        Objects.requireNonNull(request, "request");
        String payloadJson;
        try {
            payloadJson = httpClient.writeJson(request.payload());
        } catch (IOException e) {
            throw new DkbSignException("Failed to serialize envelope payload", e);
        }

        List<MultipartPart> parts = new ArrayList<>();
        for (Path document : request.documents()) {
            parts.add(MultipartPart.file("documents[]", document));
        }
        parts.add(MultipartPart.jsonText("payload", payloadJson));

        for (Map.Entry<Integer, List<Path>> entry : request.attachmentsByDocument().entrySet()) {
            String field = "attachments_" + entry.getKey() + "[]";
            for (Path attachment : entry.getValue()) {
                parts.add(MultipartPart.file(field, attachment));
            }
        }

        return httpClient.postMultipart(ENVELOPES_PATH, parts, CreateEnvelopeResponse.class);
    }


    public EnvelopeListResult listSent() {
        return list(ENVELOPES_PATH);
    }

    /** Envelopes where the connected user is a signer. */
    public EnvelopeListResult listReceived() {
        return list(ENVELOPES_PATH + "/received");
    }

    public EnvelopeDetail get(String batchId) {
        return httpClient.getJson(envelopePath(batchId), null, EnvelopeDetail.class);
    }

    public void cancel(String batchId) {
        cancel(batchId, null);
    }

    public void cancel(String batchId, String reason) {
        String path = envelopePath(batchId) + "/cancel";
        if (reason == null || reason.isBlank()) {
            httpClient.postJson(path, Collections.emptyMap(), Void.class);
        } else {
            httpClient.postJson(path, Map.of("reason", reason.trim()), Void.class);
        }
    }

    /** Deletes a sent envelope when it is in a terminal state. */
    public void deleteSent(String batchId) {
        httpClient.delete(envelopePath(batchId));
    }

    /** Hides a received envelope from the inbox when it is terminal. */
    public void hideReceived(String signerUuid) {
        Objects.requireNonNull(signerUuid, "signerUuid");
        httpClient.delete(ENVELOPES_PATH + "/received/" + signerUuid.trim());
    }

    /** Restores post-completion read access for signers. */
    public void restoreAccess(String batchId) {
        httpClient.postJson(envelopePath(batchId) + "/access", Collections.emptyMap(), Void.class);
    }

    /** Clears the sent or received inbox. */
    public void clear(EnvelopeBox box) {
        Objects.requireNonNull(box, "box");
        httpClient.postJson(ENVELOPES_PATH + "/clear", Map.of("box", box.getValue()), Void.class);
    }

    public void clear() {
        clear(EnvelopeBox.SENT);
    }

    public byte[] downloadDocument(String documentUuid) {
        Objects.requireNonNull(documentUuid, "documentUuid");
        return httpClient.getBytes(ENVELOPES_PATH + "/documents/" + documentUuid.trim() + "/file");
    }

    public void downloadDocument(String documentUuid, Path target) {
        Objects.requireNonNull(target, "target");
        try {
            Files.write(target, downloadDocument(documentUuid));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write " + target, e);
        }
    }

    public byte[] identityProof(String batchUuid, String signerUuid) {
        Objects.requireNonNull(batchUuid, "batchUuid");
        Objects.requireNonNull(signerUuid, "signerUuid");
        String path = ENVELOPES_PATH + "/"
                + batchUuid.trim()
                + "/signers/"
                + signerUuid.trim()
                + "/identity-proof";
        return httpClient.getBytes(path);
    }

    private EnvelopeListResult list(String path) {
        JsonNode body = httpClient.getJson(path, null, JsonNode.class);
        return new EnvelopeListResult(body);
    }

    private static String envelopePath(String batchId) {
        if (batchId == null || batchId.isBlank()) {
            throw new IllegalArgumentException("batchId is required");
        }
        return ENVELOPES_PATH + "/" + batchId.trim();
    }
}
