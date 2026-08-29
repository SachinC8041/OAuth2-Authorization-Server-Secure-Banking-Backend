package com.example.OAuthBankingBackendApplication.service;

import com.example.OAuthBankingBackendApplication.constants.ApplicationConstants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Round-trip tests for token creation and verification. No Spring context.
 */
class JwtServiceTest {

    private static final String TEST_SECRET = "test-secret-that-is-long-enough-for-hs256-signing";

    private JwtService serviceWithSecret(String secret, String... activeProfiles) {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(ApplicationConstants.JWT_SECRET_KEY, secret);
        environment.setActiveProfiles(activeProfiles);
        return new JwtService(environment);
    }

    private Authentication authenticated(String username, String... authorities) {
        List<GrantedAuthority> granted = List.of(authorities).stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
        return new UsernamePasswordAuthenticationToken(username, null, granted);
    }

    @Test
    @DisplayName("a generated token can be parsed back into the same principal")
    void generatedTokenRoundTrips() {
        JwtService jwtService = serviceWithSecret(TEST_SECRET);

        String token = jwtService.generateToken(authenticated("sachin@example.com", "ROLE_USER", "ROLE_ADMIN"));
        Authentication parsed = jwtService.parseToken(token);

        assertThat(parsed.getName()).isEqualTo("sachin@example.com");
        assertThat(parsed.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        assertThat(parsed.getCredentials()).isNull();
    }

    @Test
    @DisplayName("a principal with no authorities round-trips to an empty authority list")
    void tokenWithoutAuthoritiesRoundTrips() {
        JwtService jwtService = serviceWithSecret(TEST_SECRET);

        String token = jwtService.generateToken(authenticated("nobody@example.com"));

        assertThat(jwtService.parseToken(token).getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("a token signed with a different secret is rejected")
    void tokenSignedWithAnotherKeyIsRejected() {
        String token = serviceWithSecret(TEST_SECRET)
                .generateToken(authenticated("sachin@example.com", "ROLE_USER"));

        JwtService otherService = serviceWithSecret("a-completely-different-secret-value-for-hs256");

        assertThatThrownBy(() -> otherService.parseToken(token))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }

    @Test
    @DisplayName("a tampered token is rejected")
    void tamperedTokenIsRejected() {
        JwtService jwtService = serviceWithSecret(TEST_SECRET);
        String token = jwtService.generateToken(authenticated("sachin@example.com", "ROLE_USER"));

        String tampered = token.substring(0, token.length() - 4) + "AAAA";

        assertThatThrownBy(() -> jwtService.parseToken(tampered))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }

    @Test
    @DisplayName("garbage in the Authorization header is rejected rather than parsed")
    void malformedTokenIsRejected() {
        JwtService jwtService = serviceWithSecret(TEST_SECRET);

        assertThatThrownBy(() -> jwtService.parseToken("not-a-jwt"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("the development default secret is refused under the prod profile")
    void developmentSecretIsRefusedInProd() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");

        assertThatThrownBy(() -> new JwtService(environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(ApplicationConstants.JWT_SECRET_KEY);
    }

    @Test
    @DisplayName("the development default secret is allowed outside prod")
    void developmentSecretIsAllowedOutsideProd() {
        MockEnvironment environment = new MockEnvironment();

        JwtService jwtService = new JwtService(environment);

        assertThat(jwtService.generateToken(authenticated("dev@example.com"))).isNotBlank();
    }
}
