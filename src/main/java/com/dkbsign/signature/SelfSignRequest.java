package com.dkbsign.signature;

import com.dkbsign.enums.SignatureLevel;
import com.dkbsign.model.SignPage;
import com.dkbsign.model.SignatureMark;

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


public final class SelfSignRequest {

    private final List<Path> files;
    private final Path signatureImage;
    private final SignatureLevel level;
    private final List<SignPage> pages;
    private final String otpCode;
    private final String mfaCode;
    private final String secondOtpCode;

    private SelfSignRequest(Builder builder) {
        this.files = List.copyOf(builder.files);
        this.signatureImage = builder.signatureImage;
        this.level = builder.level;
        this.otpCode = builder.otpCode;
        this.mfaCode = builder.mfaCode;
        this.secondOtpCode = builder.secondOtpCode;

        List<SignPage> result = new ArrayList<>();
        builder.marksByPage.forEach((page, marks) -> result.add(new SignPage(page, marks)));
        this.pages = List.copyOf(result);
    }

    public static Builder builder() {
        return new Builder();
    }


    public List<Path> files() {
        return files;
    }


    public Path signatureImage() {
        return signatureImage;
    }


    public SignatureLevel level() {
        return level;
    }

    public List<SignPage> pages() {
        return pages;
    }


    public String otpCode() {
        return otpCode;
    }


    public String mfaCode() {
        return mfaCode;
    }


    public String secondOtpCode() {
        return secondOtpCode;
    }

    public static final class Builder {

        private static final long MAX_PDF_BYTES = 10L * 1024 * 1024;
        private static final long MAX_IMAGE_BYTES = 2L * 1024 * 1024;
        private static final int MAX_FILES = 100;

        private final List<Path> files = new ArrayList<>();
        private Path signatureImage;
        private SignatureLevel level;
        private final Map<Integer, List<SignatureMark>> marksByPage = new LinkedHashMap<>();
        private String otpCode;
        private String mfaCode;
        private String secondOtpCode;

        private Builder() {}


        public Builder file(Path path) {
            Path checked = requirePdf(path);
            files.clear();
            files.add(checked);
            return this;
        }

        public Builder file(String path) {
            return file(Path.of(path));
        }

        /** Adds a PDF to sign with {@link SelfSignService#signAll}; max 100 files, 10 MB each. */
        public Builder addFile(Path path) {
            Path checked = requirePdf(path);
            if (files.size() >= MAX_FILES) {
                throw new IllegalArgumentException("At most " + MAX_FILES + " PDF files per request");
            }
            files.add(checked);
            return this;
        }

        public Builder addFile(String path) {
            return addFile(Path.of(path));
        }

        /** Replaces the PDFs to sign with {@code paths}. */
        public Builder files(List<Path> paths) {
            Objects.requireNonNull(paths, "paths");
            if (paths.size() > MAX_FILES) {
                throw new IllegalArgumentException("At most " + MAX_FILES + " PDF files per request");
            }
            List<Path> checked = new ArrayList<>(paths.size());
            for (Path path : paths) {
                checked.add(requirePdf(path));
            }
            files.clear();
            files.addAll(checked);
            return this;
        }

        private static Path requirePdf(Path path) {
            return requireFile(path, "PDF file", MAX_PDF_BYTES, ".pdf");
        }

        /** Optional PNG/JPG image of the signature, max 2 MB. */
        public Builder signatureImage(Path path) {
            this.signatureImage =
                    requireFile(path, "Signature image", MAX_IMAGE_BYTES, ".png", ".jpg", ".jpeg");
            return this;
        }

        public Builder signatureImage(String path) {
            return signatureImage(Path.of(path));
        }

        public Builder level(SignatureLevel level) {
            this.level = Objects.requireNonNull(level, "level");
            return this;
        }


        public Builder mark(int page, List<SignatureMark> marks) {
            if (page < 0) {
                throw new IllegalArgumentException("page must be >= 0");
            }
            return addMarks(page, marks);
        }

        public Builder mark(int page, SignatureMark... marks) {
            return mark(page, marks == null ? null : List.of(marks));
        }


        public Builder mark(List<SignatureMark> marks) {
            return addMarks(null, marks);
        }

        public Builder mark(SignatureMark... marks) {
            return mark(marks == null ? null : List.of(marks));
        }

        private Builder addMarks(Integer page, List<SignatureMark> marks) {
            if (marks == null || marks.isEmpty()) {
                throw new IllegalArgumentException("At least one signature mark is required");
            }
            for (SignatureMark mark : marks) {
                Objects.requireNonNull(mark, "signature mark");
            }
            // LinkedHashMap accepts the null key used for page-less anchor marks.
            marksByPage.computeIfAbsent(page, p -> new ArrayList<>()).addAll(marks);
            return this;
        }

        /** Code received after {@link SelfSignService#sendOtp()}. */
        public Builder otpCode(String otpCode) {
            this.otpCode = blankToNull(otpCode);
            return this;
        }

        /** TOTP code, for a qualified signature when the user has an authenticator app. */
        public Builder mfaCode(String mfaCode) {
            this.mfaCode = blankToNull(mfaCode);
            return this;
        }

        /** Code received after {@link SelfSignService#sendSecondOtp()}, for a qualified signature without TOTP. */
        public Builder secondOtpCode(String secondOtpCode) {
            this.secondOtpCode = blankToNull(secondOtpCode);
            return this;
        }

        private static String blankToNull(String value) {
            if (value == null) {
                return null;
            }
            String trimmed = value.trim();
            return trimmed.isEmpty() ? null : trimmed;
        }

        public SelfSignRequest build() {
            if (files.isEmpty()) {
                throw new IllegalStateException("file() or addFile() is required");
            }
            if (marksByPage.isEmpty()) {
                throw new IllegalStateException("At least one mark() is required");
            }
            return new SelfSignRequest(this);
        }

        private static Path requireFile(
                Path path, String label, long maxBytes, String... extensions) {
            if (path == null) {
                throw new IllegalArgumentException(label + " path is required");
            }
            if (!Files.isRegularFile(path)) {
                throw new IllegalArgumentException(label + " not found: " + path);
            }

            String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
            boolean allowed = false;
            for (String extension : extensions) {
                allowed |= name.endsWith(extension);
            }
            if (!allowed) {
                throw new IllegalArgumentException(
                        label + " must be " + String.join(", ", extensions) + ": " + path);
            }

            long size;
            try {
                size = Files.size(path);
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot read " + label + ": " + path, e);
            }
            if (size > maxBytes) {
                throw new IllegalArgumentException(String.format(
                        Locale.ROOT, "%s exceeds %d MB (%.1f MB): %s",
                        label, maxBytes / (1024 * 1024), size / (1024.0 * 1024), path));
            }
            return path;
        }
    }
}
