package com.example.OAuthBankingBackendApplication.filter;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class RequestValidationBeforeFilterTest {

    private RequestValidationBeforeFilter filter;
    private MockHttpServletResponse response;
    private MockFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new RequestValidationBeforeFilter();
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
    }

    private static MockHttpServletRequest requestWithBasic(String rawCredentials) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/account");
        String encoded = Base64.getEncoder()
                .encodeToString(rawCredentials.getBytes(StandardCharsets.UTF_8));
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        return request;
    }

    private static boolean chainWasCalled(FilterChain chain) {
        return ((MockFilterChain) chain).getRequest() != null;
    }

    @Test
    @DisplayName("a request with no Authorization header passes through")
    void noHeaderPassesThrough() throws Exception {
        filter.doFilter(new MockHttpServletRequest("GET", "/notices"), response, chain);

        assertThat(chainWasCalled(chain)).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("a Bearer token is left alone for the JWT filter to handle")
    void bearerHeaderPassesThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/account");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer some.jwt.value");

        filter.doFilter(request, response, chain);

        assertThat(chainWasCalled(chain)).isTrue();
    }

    @Test
    @DisplayName("valid Basic credentials pass through")
    void validCredentialsPassThrough() throws Exception {
        filter.doFilter(requestWithBasic("sachin@example.com:Password@12345"), response, chain);

        assertThat(chainWasCalled(chain)).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("a username containing 'test' is refused with 400")
    void bannedUsernameIsRefused() throws Exception {
        filter.doFilter(requestWithBasic("test@example.com:Password@12345"), response, chain);

        assertThat(chainWasCalled(chain)).isFalse();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains("not permitted");
    }

    @Test
    @DisplayName("the ban on 'test' is case-insensitive")
    void bannedUsernameIsCaseInsensitive() throws Exception {
        filter.doFilter(requestWithBasic("TEST@example.com:Password@12345"), response, chain);

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("credentials with no colon are refused with 400, not a 500")
    void missingSeparatorIsRefused() throws Exception {
        filter.doFilter(requestWithBasic("no-separator-here"), response, chain);

        assertThat(chainWasCalled(chain)).isFalse();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains("Invalid basic authentication token");
    }

    @Test
    @DisplayName("a non-Base64 payload is refused with 400, not a 500")
    void undecodablePayloadIsRefused() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/account");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic !!!not-base64!!!");

        filter.doFilter(request, response, chain);

        assertThat(chainWasCalled(chain)).isFalse();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains("Failed to decode");
    }
}
