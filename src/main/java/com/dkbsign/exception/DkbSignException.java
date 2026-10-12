package com.dkbsign.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DkbSignException extends RuntimeException {

    private static final int MAX_BODY_IN_MESSAGE = 500;

    private final int statusCode;
    private final String responseBody;
    private final String errorCode;
    private final String apiMessage;
    private final Map<String, Object> fieldErrors;

    public DkbSignException(
            String message,
            int statusCode,
            String responseBody,
            String errorCode,
            String apiMessage,
            Map<String, Object> fieldErrors) {
        super(formatMessage(message, errorCode, apiMessage, responseBody));
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.errorCode = errorCode;
        this.apiMessage = apiMessage;
        // Map.copyOf rejects null values, which a JSON "errors" object may contain.
        this.fieldErrors = fieldErrors == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(fieldErrors));
    }

    public DkbSignException(
            String message,
            int statusCode,
            String responseBody) {

        this(message, statusCode, responseBody, null, null, null);
    }

    public DkbSignException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.responseBody = null;
        this.errorCode = null;
        this.apiMessage = null;
        this.fieldErrors = Map.of();
    }

    private static String formatMessage(
            String message,
            String errorCode,
            String apiMessage,
            String responseBody) {

        StringBuilder result = new StringBuilder(message);

        if (errorCode != null && !errorCode.isBlank()) {
            result.append(" [").append(errorCode).append(']');
        }

        String detail = apiMessage;

        if (detail == null || detail.isBlank()) {
            detail = truncate(responseBody);
        }

        if (detail != null && !detail.isBlank()) {
            result.append(" — ").append(detail);
        }

        return result.toString();
    }

    private static String truncate(String value) {
        if (value == null || value.length() <= MAX_BODY_IN_MESSAGE) {
            return value;
        }
        return value.substring(0, MAX_BODY_IN_MESSAGE) + "…";
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getApiMessage() {
        return apiMessage;
    }

    public Map<String, Object> getFieldErrors() {
        return fieldErrors;
    }

    public boolean isUnauthorized() {
        return statusCode == 401;
    }

    public boolean isRetryable() {
        return statusCode == 503;
    }
}
