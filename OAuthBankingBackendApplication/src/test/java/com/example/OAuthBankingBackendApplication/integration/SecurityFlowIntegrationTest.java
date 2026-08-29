package com.example.OAuthBankingBackendApplication.integration;

import com.example.OAuthBankingBackendApplication.constants.ApplicationConstants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the whole security chain against an in-memory database: the real
 * filter order, the real authorization rules, the real token.
 *
 * <p>Seed data lives in {@code src/test/resources/data.sql}. Two customers exist
 * so that the ownership check has somebody to be denied on behalf of.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityFlowIntegrationTest {

    private static final String ALICE = "alice@example.com";
    private static final String ALICE_PASSWORD = "Password@12345";
    private static final long ALICE_ID = 1L;
    private static final long BOB_ID = 2L;

    @Autowired
    private MockMvc mockMvc;

    // ------------------------------------------------------------------
    // Public endpoints
    // ------------------------------------------------------------------

    @Test
    @DisplayName("/notices is reachable without credentials")
    void noticesArePublic() throws Exception {
        mockMvc.perform(get("/notices")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("/account is refused without credentials, with a JSON 401 body")
    void protectedEndpointRequiresCredentials() throws Exception {
        mockMvc.perform(get("/account").param("id", String.valueOf(ALICE_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/account"));
    }

    // ------------------------------------------------------------------
    // Login
    // ------------------------------------------------------------------

    @Test
    @DisplayName("valid credentials return a token in both the header and the body")
    void loginIssuesToken() throws Exception {
        MvcResult result = login(ALICE, ALICE_PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken").isNotEmpty())
                .andReturn();

        assertThat(result.getResponse().getHeader(ApplicationConstants.JWT_HEADER)).isNotBlank();
    }

    @Test
    @DisplayName("a wrong password returns 401 rather than 200 with an empty token")
    void loginRejectsWrongPassword() throws Exception {
        login(ALICE, "definitely-not-the-password").andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("an unknown user returns 401")
    void loginRejectsUnknownUser() throws Exception {
        login("ghost@example.com", ALICE_PASSWORD).andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------
    // Token usage
    // ------------------------------------------------------------------

    @Test
    @DisplayName("a token issued at login opens the protected endpoints")
    void tokenOpensProtectedEndpoints() throws Exception {
        String token = tokenFor(ALICE, ALICE_PASSWORD);

        mockMvc.perform(get("/cards")
                        .param("id", String.valueOf(ALICE_ID))
                        .header(ApplicationConstants.JWT_HEADER, token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("a junk token is answered with a JSON 401, not a container error page")
    void junkTokenYields401() throws Exception {
        mockMvc.perform(get("/cards")
                        .param("id", String.valueOf(ALICE_ID))
                        .header(ApplicationConstants.JWT_HEADER, "not.a.real.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // ------------------------------------------------------------------
    // Ownership
    // ------------------------------------------------------------------

    @Test
    @DisplayName("a customer cannot read another customer's account by changing the id")
    void cannotReadAnotherCustomersAccount() throws Exception {
        String token = tokenFor(ALICE, ALICE_PASSWORD);

        mockMvc.perform(get("/account")
                        .param("id", String.valueOf(BOB_ID))
                        .header(ApplicationConstants.JWT_HEADER, token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("the same protection applies to cards, loans and balance")
    void ownershipIsEnforcedOnEveryDataEndpoint() throws Exception {
        String token = tokenFor(ALICE, ALICE_PASSWORD);

        for (String path : new String[]{"/cards", "/loans", "/balance"}) {
            mockMvc.perform(get(path)
                            .param("id", String.valueOf(BOB_ID))
                            .header(ApplicationConstants.JWT_HEADER, token))
                    .andExpect(status().isForbidden());
        }
    }

    // ------------------------------------------------------------------
    // HTTP Basic path
    // ------------------------------------------------------------------

    @Test
    @DisplayName("/user authenticates with Basic and hands back a freshly issued token")
    void basicAuthOnUserIssuesToken() throws Exception {
        MvcResult result = mockMvc.perform(get("/user").header(HttpHeaders.AUTHORIZATION, basic(ALICE, ALICE_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(ALICE))
                .andReturn();

        assertThat(result.getResponse().getHeader(ApplicationConstants.JWT_HEADER)).isNotBlank();
    }

    @Test
    @DisplayName("the password hash is never serialised back to the client")
    void passwordIsNeverReturned() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/user").header(HttpHeaders.AUTHORIZATION, basic(ALICE, ALICE_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).doesNotContain("pwd");
    }

    @Test
    @DisplayName("a username containing 'test' is blocked by the request validation filter")
    void bannedUsernameIsBlockedBeforeAuthentication() throws Exception {
        mockMvc.perform(get("/user").header(HttpHeaders.AUTHORIZATION, basic("test@example.com", "anything")))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------
    // Registration
    // ------------------------------------------------------------------

    @Test
    @DisplayName("registration is public and returns 201")
    void registrationIsPublic() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "New Customer",
                                  "email": "new.customer@example.com",
                                  "mobileNumber": "9876543210",
                                  "pwd": "Password@12345",
                                  "role": "user"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private org.springframework.test.web.servlet.ResultActions login(String username, String password)
            throws Exception {
        return mockMvc.perform(post("/apiLogin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"));
    }

    private String tokenFor(String username, String password) throws Exception {
        return login(username, password)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getHeader(ApplicationConstants.JWT_HEADER);
    }

    private static String basic(String username, String password) {
        String encoded = Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}
