package com.dkbsign.http;

import com.fasterxml.jackson.databind.JsonNode;

/** Successful (2xx) API response with its HTTP status and untyped JSON body. */
public record ApiResponse(int statusCode, JsonNode body) {
}
