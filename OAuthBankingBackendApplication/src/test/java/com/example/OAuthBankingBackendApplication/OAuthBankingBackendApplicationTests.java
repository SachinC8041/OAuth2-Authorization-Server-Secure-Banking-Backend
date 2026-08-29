package com.example.OAuthBankingBackendApplication;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: every bean wires up and the context starts.
 *
 * <p>Runs under the {@code test} profile so it uses the in-memory database rather
 * than looking for a MySQL server. Without that, this test failed on any machine
 * where MySQL was not running, which is what made it useless as a first check.
 */
@SpringBootTest
@ActiveProfiles("test")
class OAuthBankingBackendApplicationTests {

    @Test
    @DisplayName("the application context loads")
    void contextLoads() {
        // The test passes if the context above started without error.
    }
}
