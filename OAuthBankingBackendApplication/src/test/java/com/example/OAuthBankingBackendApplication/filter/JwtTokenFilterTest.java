package com.example.OAuthBankingBackendApplication.filter;

import com.example.OAuthBankingBackendApplication.constants.ApplicationConstants;
import com.example.OAuthBankingBackendApplication.security.CustomAuthenticationEntryPoint;
import com.example.OAuthBankingBackendApplication.service.JwtService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenFilterTest {

    private static final String TEST_SECRET = "test-secret-that-is-long-enough-for-hs256-signing";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(ApplicationConstants.JWT_SECRET_KEY, TEST_SECRET);
        jwtService = new JwtService(environment);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private Authentication authenticated() {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        return new UsernamePasswordAuthenticationToken("sachin@example.com", null, authorities);
    }

    // ------------------------------------------------------------------
    // Generator
    // ------------------------------------------------------------------

    @Test
    @DisplayName("the generator writes a token onto the response when authenticated")
    void generatorWritesToken() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(authenticated());
        MockHttpServletResponse response = new MockHttpServletResponse();

        new JwtTokenGeneratorFilter(jwtService)
                .doFilter(new MockHttpServletRequest("GET", "/user"), response, new MockFilterChain());

        assertThat(response.getHeader(ApplicationConstants.JWT_HEADER)).isNotBlank();
    }

    @Test
    @DisplayName("the generator writes nothing when there is no authentication")
    void generatorSkipsAnonymous() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new JwtTokenGeneratorFilter(jwtService)
                .doFilter(new MockHttpServletRequest("GET", "/user"), response, new MockFilterChain());

        assertThat(response.getHeader(ApplicationConstants.JWT_HEADER)).isNull();
    }

    @Test
    @DisplayName("the generator runs on /user only")
    void generatorOnlyRunsOnUser() {
        JwtTokenGeneratorFilter filter = new JwtTokenGeneratorFilter(jwtService);

        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/user"))).isFalse();
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/account"))).isTrue();
    }

    // ------------------------------------------------------------------
    // Validator
    // ------------------------------------------------------------------

    @Test
    @DisplayName("a valid token populates the security context")
    void validatorAcceptsGoodToken() throws Exception {
        String token = jwtService.generateToken(authenticated());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/account");
        request.addHeader(ApplicationConstants.JWT_HEADER, token);
        MockFilterChain chain = new MockFilterChain();

        validator().doFilter(request, new MockHttpServletResponse(), chain);

        Authentication result = SecurityContextHolder.getContext().getAuthentication();
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("sachin@example.com");
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("a request with no token passes through without authenticating")
    void validatorPassesThroughWithoutToken() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        validator().doFilter(
                new MockHttpServletRequest("GET", "/notices"), new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("an invalid token produces a 401 instead of escaping the filter chain")
    void validatorRejectsBadTokenWith401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/account");
        request.addHeader(ApplicationConstants.JWT_HEADER, "clearly.not.a.valid.token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        validator().doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(chain.getRequest()).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("a token signed with another key produces a 401")
    void validatorRejectsForeignToken() throws Exception {
        MockEnvironment other = new MockEnvironment();
        other.setProperty(ApplicationConstants.JWT_SECRET_KEY, "a-totally-different-secret-value-here!");
        String foreignToken = new JwtService(other).generateToken(authenticated());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/account");
        request.addHeader(ApplicationConstants.JWT_HEADER, foreignToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        validator().doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("the validator skips /user, which authenticates with Basic")
    void validatorSkipsUser() {
        assertThat(validator().shouldNotFilter(new MockHttpServletRequest("GET", "/user"))).isTrue();
        assertThat(validator().shouldNotFilter(new MockHttpServletRequest("GET", "/account"))).isFalse();
    }

    private JwtTokenValidatorFilter validator() {
        return new JwtTokenValidatorFilter(jwtService, new CustomAuthenticationEntryPoint());
    }
}
