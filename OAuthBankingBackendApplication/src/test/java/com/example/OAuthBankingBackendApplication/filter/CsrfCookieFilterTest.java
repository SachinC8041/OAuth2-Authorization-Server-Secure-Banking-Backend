package com.example.OAuthBankingBackendApplication.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class CsrfCookieFilterTest {

    @Test
    @DisplayName("the token is resolved when the request attribute is present")
    void resolvesTheToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/user");
        CsrfToken token = new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "token-value");
        request.setAttribute(CsrfToken.class.getName(), token);
        MockFilterChain chain = new MockFilterChain();

        new CsrfCookieFilter().doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(token.getToken()).isEqualTo("token-value");
    }

    @Test
    @DisplayName("a missing attribute no longer causes a NullPointerException")
    void missingAttributeIsTolerated() {
        MockFilterChain chain = new MockFilterChain();

        assertThatCode(() -> new CsrfCookieFilter().doFilter(
                new MockHttpServletRequest("GET", "/notices"), new MockHttpServletResponse(), chain))
                .doesNotThrowAnyException();

        assertThat(chain.getRequest()).isNotNull();
    }
}
