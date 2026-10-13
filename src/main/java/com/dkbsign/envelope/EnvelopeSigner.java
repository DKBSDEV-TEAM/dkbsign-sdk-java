package com.dkbsign.envelope;

import com.dkbsign.model.SignatureMark;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class EnvelopeSigner {

    private final String email;
    private final String name;
    private final String firstName;
    private final String phone;
    private final Integer priority;

    private final Map<String, List<SignatureMark>> positions;

    private EnvelopeSigner(Builder builder) {
        this.email = builder.email;
        this.name = builder.name;
        this.firstName = builder.firstName;
        this.phone = builder.phone;
        this.priority = builder.priority;
        Map<String, List<SignatureMark>> copy = new LinkedHashMap<>();
        builder.positions.forEach((key, marks) -> copy.put(key, List.copyOf(marks)));
        this.positions = Map.copyOf(copy);
    }

    public static Builder builder(String email) {
        return new Builder(email);
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("first_name")
    public String getFirstName() {
        return firstName;
    }

    @JsonProperty("phone")
    public String getPhone() {
        return phone;
    }

    @JsonProperty("priority")
    public Integer getPriority() {
        return priority;
    }

    @JsonProperty("positions")
    public Map<String, List<SignatureMark>> getPositions() {
        return positions.isEmpty() ? null : positions;
    }

    public static final class Builder {

        private final String email;
        private String name;
        private String firstName;
        private String phone;
        private Integer priority;
        private final Map<String, List<SignatureMark>> positions = new LinkedHashMap<>();

        private Builder(String email) {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("email is required");
            }
            this.email = email.trim();
        }

        public Builder name(String name) {
            this.name = blankToNull(name);
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = blankToNull(firstName);
            return this;
        }

        /** E.164 format, e.g. {@code +2250700000001}. */
        public Builder phone(String phone) {
            this.phone = blankToNull(phone);
            return this;
        }

        public Builder priority(int priority) {
            if (priority < 1) {
                throw new IllegalArgumentException("priority must be >= 1");
            }
            this.priority = priority;
            return this;
        }

        /**
         * Adds a mark on document {@code documentIndex}. Each mark must include {@code page}
         * for coordinate placement (use {@link SignatureMark#onPage(int)} on anchor marks,
         * or {@link #mark(int, int, SignatureMark)}).
         */
        public Builder mark(String documentIndex, SignatureMark mark) {
            Objects.requireNonNull(mark, "mark");
            if (documentIndex == null || documentIndex.isBlank()) {
                throw new IllegalArgumentException("documentIndex is required");
            }
            positions.computeIfAbsent(documentIndex, k -> new ArrayList<>()).add(mark);
            return this;
        }


        public Builder mark(int documentIndex, SignatureMark mark) {
            return mark(String.valueOf(documentIndex), mark);
        }

        public EnvelopeSigner build() {
            return new EnvelopeSigner(this);
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
