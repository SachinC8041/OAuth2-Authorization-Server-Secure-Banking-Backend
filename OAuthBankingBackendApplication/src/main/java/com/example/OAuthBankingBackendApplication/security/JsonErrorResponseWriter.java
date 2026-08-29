package com.example.OAuthBankingBackendApplication.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Writes the application's standard JSON error body.
 *
 * <p>Both security handlers and the security filters need to produce the same
 * error shape, and filters are not Spring beans, so this is a static helper rather
 * than an injected collaborator.
 *
 * <p>The body is assembled by hand instead of through Jackson so that this class
 * has no databind dependency and stays usable from anywhere in the filter chain.
 * The escaping below is the reason the previous {@code String.format} version was
 * replaced: an exception message containing a quote or a backslash used to emit
 * malformed JSON.
 */
public final class JsonErrorResponseWriter {

    private JsonErrorResponseWriter() {
        throw new AssertionError("JsonErrorResponseWriter is a utility class and must not be instantiated");
    }

    /**
     * Sends a JSON error response and commits it.
     *
     * @param request     the request being rejected, used for the {@code path} field
     * @param response    the response to write to
     * @param status      the HTTP status to send
     * @param headerName  name of the diagnostic response header
     * @param headerValue value of the diagnostic response header
     * @param message     human-readable reason, safe to expose to the caller
     */
    public static void write(HttpServletRequest request,
                             HttpServletResponse response,
                             HttpStatus status,
                             String headerName,
                             String headerValue,
                             String message) throws IOException {

        response.setHeader(headerName, headerValue);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String body = "{"
                + "\"timestamp\":\"" + escape(LocalDateTime.now().toString()) + "\","
                + "\"status\":" + status.value() + ","
                + "\"error\":\"" + escape(status.getReasonPhrase()) + "\","
                + "\"message\":\"" + escape(message) + "\","
                + "\"path\":\"" + escape(request.getRequestURI()) + "\""
                + "}";

        response.getWriter().write(body);
    }

    /**
     * Escapes the characters that are illegal inside a JSON string literal.
     */
    static String escape(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
