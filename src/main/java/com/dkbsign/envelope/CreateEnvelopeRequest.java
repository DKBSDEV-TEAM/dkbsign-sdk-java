package com.dkbsign.envelope;

import com.dkbsign.enums.SignatureLevel;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Immutable request to create an envelope ({@code POST /api/v4/envelopes}). */
public final class CreateEnvelopeRequest {

    private final List<Path> documents;
    private final Map<Integer, List<Path>> attachmentsByDocument;
    private final EnvelopePayload payload;

    private CreateEnvelopeRequest(Builder builder) {
        this.documents = List.copyOf(builder.documents);
        Map<Integer, List<Path>> attachments = new LinkedHashMap<>();
        builder.attachments.forEach((idx, paths) -> attachments.put(idx, List.copyOf(paths)));
        this.attachmentsByDocument = Map.copyOf(attachments);
        this.payload = builder.buildPayload();
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<Path> documents() {
        return documents;
    }

    public Map<Integer, List<Path>> attachmentsByDocument() {
        return attachmentsByDocument;
    }

    public EnvelopePayload payload() {
        return payload;
    }

    public static final class Builder {

        private static final long MAX_PDF_BYTES = 10L * 1024 * 1024;
        private static final long MAX_ATTACHMENT_BYTES = 15L * 1024 * 1024;
        private static final int MAX_FILES = 100;

        private final List<Path> documents = new ArrayList<>();
        private final Map<Integer, List<Path>> attachments = new LinkedHashMap<>();

        private String title;
        private String message;
        private SigningOrder signingOrder;
        private SignatureLevel signatureLevel;
        private EnvelopePassword password;
        private final List<EnvelopeSigner> signers = new ArrayList<>();

        private Builder() {}

        public Builder document(Path path) {
            documents.add(requirePdf(path));
            return this;
        }

        public Builder document(String path) {
            return document(Path.of(path));
        }

        public Builder attachment(int documentIndex, Path path) {
            if (documentIndex < 0) {
                throw new IllegalArgumentException("documentIndex must be >= 0");
            }
            attachments.computeIfAbsent(documentIndex, i -> new ArrayList<>())
                    .add(requireAttachment(path));
            return this;
        }

        public Builder attachment(int documentIndex, String path) {
            return attachment(documentIndex, Path.of(path));
        }

        public Builder title(String title) {
            this.title = blankToNull(title);
            return this;
        }

        public Builder message(String message) {
            this.message = blankToNull(message);
            return this;
        }

        public Builder signingOrder(SigningOrder signingOrder) {
            this.signingOrder = signingOrder;
            return this;
        }

        public Builder signatureLevel(SignatureLevel signatureLevel) {
            this.signatureLevel = signatureLevel;
            return this;
        }

        public Builder password(EnvelopePassword password) {
            this.password = password;
            return this;
        }

        public Builder signer(EnvelopeSigner signer) {
            signers.add(Objects.requireNonNull(signer, "signer"));
            return this;
        }

        public CreateEnvelopeRequest build() {
            if (documents.isEmpty()) {
                throw new IllegalStateException("At least one document() is required");
            }
            if (signers.isEmpty()) {
                throw new IllegalStateException("At least one signer() is required");
            }
            validateDocumentReferences();
            validateAttachments();
            if (signingOrder == SigningOrder.ORDERED) {
                validateOrderedPriorities();
            }
            return new CreateEnvelopeRequest(this);
        }

        private EnvelopePayload buildPayload() {
            EnvelopePayload.Builder payload = EnvelopePayload.builder().title(title);
            if (message != null) {
                payload.message(message);
            }
            if (signingOrder != null) {
                payload.signingOrder(signingOrder);
            }
            if (signatureLevel != null) {
                payload.signatureLevel(signatureLevel);
            }
            if (password != null) {
                payload.password(password);
            }
            signers.forEach(payload::signer);
            return payload.build();
        }

        private void validateDocumentReferences() {
            int docCount = documents.size();
            for (EnvelopeSigner signer : signers) {
                if (signer.getPositions() == null) {
                    continue;
                }
                for (String key : signer.getPositions().keySet()) {
                    int index;
                    try {
                        index = Integer.parseInt(key);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                                "Invalid positions key (expected document index): " + key);
                    }
                    if (index < 0 || index >= docCount) {
                        throw new IllegalArgumentException(String.format(
                                Locale.ROOT,
                                "Signer %s references document index %d but only %d document(s) were added",
                                signer.getEmail(),
                                index,
                                docCount));
                    }
                }
            }
        }

        private void validateAttachments() {
            int docCount = documents.size();
            for (Integer index : attachments.keySet()) {
                if (index < 0 || index >= docCount) {
                    throw new IllegalArgumentException(String.format(
                            Locale.ROOT,
                            "attachment() references document index %d but only %d document(s) were added",
                            index,
                            docCount));
                }
            }
        }

        private void validateOrderedPriorities() {
            long withPriority = signers.stream().filter(s -> s.getPriority() != null).count();
            if (withPriority > 0 && withPriority < signers.size()) {
                throw new IllegalArgumentException(
                        "When signing_order is ordered, every signer must have priority() or none");
            }
        }

        private static Path requirePdf(Path path) {
            return requireFile(path, "PDF document", MAX_PDF_BYTES, ".pdf");
        }

        private static Path requireAttachment(Path path) {
            return requireFile(path, "Attachment", MAX_ATTACHMENT_BYTES);
        }

        private static Path requireFile(Path path, String label, long maxBytes, String... extensions) {
            if (path == null) {
                throw new IllegalArgumentException(label + " path is required");
            }
            if (!Files.isRegularFile(path)) {
                throw new IllegalArgumentException(label + " not found: " + path);
            }
            if (extensions.length > 0) {
                String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                boolean allowed = false;
                for (String extension : extensions) {
                    allowed |= name.endsWith(extension);
                }
                if (!allowed) {
                    throw new IllegalArgumentException(
                            label + " must be " + String.join(", ", extensions) + ": " + path);
                }
            }
            long size;
            try {
                size = Files.size(path);
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot read " + label + ": " + path, e);
            }
            if (size > maxBytes) {
                throw new IllegalArgumentException(String.format(
                        Locale.ROOT,
                        "%s exceeds %d MB (%.1f MB): %s",
                        label,
                        maxBytes / (1024 * 1024),
                        size / (1024.0 * 1024),
                        path));
            }
            return path;
        }

        private static String blankToNull(String value) {
            if (value == null) {
                return null;
            }
            String trimmed = value.trim();
            return trimmed.isEmpty() ? null : trimmed;
        }
    }
}
