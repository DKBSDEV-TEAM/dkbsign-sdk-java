package com.dkbsign.publicenvelope;

import java.nio.file.Path;
import java.util.List;

/** Multipart body for {@code POST .../identity/enroll}. */
public final class PublicIdentityEnrollRequest {

    private final Path document;
    private final IdentityDocumentType documentType;
    private final List<Path> selfieFrames;
    private final String documentNumberLast4;

    private PublicIdentityEnrollRequest(Builder builder) {
        this.document = builder.document;
        this.documentType = builder.documentType;
        this.selfieFrames = List.copyOf(builder.selfieFrames);
        this.documentNumberLast4 = builder.documentNumberLast4;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Path document() {
        return document;
    }

    public IdentityDocumentType documentType() {
        return documentType;
    }

    public List<Path> selfieFrames() {
        return selfieFrames;
    }

    public String documentNumberLast4() {
        return documentNumberLast4;
    }

    public static final class Builder {

        private Path document;
        private IdentityDocumentType documentType;
        private final java.util.ArrayList<Path> selfieFrames = new java.util.ArrayList<>();
        private String documentNumberLast4;

        public Builder document(Path path) {
            this.document = path;
            return this;
        }

        public Builder documentType(IdentityDocumentType type) {
            this.documentType = type;
            return this;
        }

        public Builder addSelfieFrame(Path path) {
            this.selfieFrames.add(path);
            return this;
        }

        public Builder documentNumberLast4(String last4) {
            this.documentNumberLast4 = last4;
            return this;
        }

        public PublicIdentityEnrollRequest build() {
            if (document == null) {
                throw new IllegalStateException("document() is required");
            }
            if (documentType == null) {
                throw new IllegalStateException("documentType() is required");
            }
            if (selfieFrames.size() < 3 || selfieFrames.size() > 8) {
                throw new IllegalStateException("Between 3 and 8 selfie_frames are required");
            }
            return new PublicIdentityEnrollRequest(this);
        }
    }
}
