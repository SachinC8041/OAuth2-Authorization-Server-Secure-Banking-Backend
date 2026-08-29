package com.example.OAuthBankingBackendApplication.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class JsonErrorResponseWriterTest {

    @Test
    @DisplayName("the body carries the status, message and request path")
    void writesTheExpectedFields() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/account");
        MockHttpServletResponse response = new MockHttpServletResponse();

        JsonErrorResponseWriter.write(
                request, response, HttpStatus.FORBIDDEN, "bank-error-reason", "Authorization failed", "Access denied");

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getHeader("bank-error-reason")).isEqualTo("Authorization failed");
        assertThat(response.getContentType()).contains("application/json");
        assertThat(response.getContentAsString())
                .contains("\"status\":403")
                .contains("\"error\":\"Forbidden\"")
                .contains("\"message\":\"Access denied\"")
                .contains("\"path\":\"/account\"");
    }

    @Test
    @DisplayName("a quote in the message is escaped rather than breaking the JSON")
    void escapesQuotes() {
        assertThat(JsonErrorResponseWriter.escape("he said \"no\"")).isEqualTo("he said \\\"no\\\"");
    }

    @Test
    @DisplayName("backslashes and control characters are escaped")
    void escapesBackslashesAndControlCharacters() {
        assertThat(JsonErrorResponseWriter.escape("a\\b")).isEqualTo("a\\\\b");
        assertThat(JsonErrorResponseWriter.escape("line1\nline2")).isEqualTo("line1\\nline2");
        assertThat(JsonErrorResponseWriter.escape("tab\there")).isEqualTo("tab\\there");
        assertThat(JsonErrorResponseWriter.escape("bell\u0007")).isEqualTo("bell\\u0007");
    }

    @Test
    @DisplayName("a null value escapes to the empty string")
    void escapesNull() {
        assertThat(JsonErrorResponseWriter.escape(null)).isEmpty();
    }

    @Test
    @DisplayName("a message full of quotes still produces parseable JSON")
    void producesParseableJsonForHostileMessages() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/user");
        MockHttpServletResponse response = new MockHttpServletResponse();

        JsonErrorResponseWriter.write(
                request, response, HttpStatus.UNAUTHORIZED, "h", "v", "bad \"quoted\" \\ value");

        String body = response.getContentAsString();
        assertThat(body).startsWith("{").endsWith("}");
        // Every quote is either a delimiter or escaped, so the count stays even.
        assertThat(body.chars().filter(c -> c == '"').count() % 2).isZero();
    }
}
