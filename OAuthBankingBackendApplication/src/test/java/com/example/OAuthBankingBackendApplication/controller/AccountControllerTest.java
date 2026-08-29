package com.example.OAuthBankingBackendApplication.controller;

import com.example.OAuthBankingBackendApplication.entity.Accounts;
import com.example.OAuthBankingBackendApplication.service.AccountService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller-level behaviour with the service mocked out: status codes and the
 * mapping from an AccessDeniedException to a 403.
 *
 * <p>The ownership rule itself is tested in {@code AccountServiceTest}, and the
 * whole path is tested in {@code SecurityFlowIntegrationTest}.
 */
@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    private static Accounts account() {
        Accounts accounts = new Accounts();
        accounts.setAccountNumber(1865764534L);
        accounts.setCustomerId(1L);
        accounts.setAccountType("Savings");
        accounts.setBranchAddress("123 Main Street, Nanded");
        return accounts;
    }

    @Test
    @WithMockUser(username = "sachin@example.com", roles = "USER")
    @DisplayName("a customer can read their own account")
    void returnsOwnAccount() throws Exception {
        when(accountService.findAccountFor(any(), eq(1L))).thenReturn(Optional.of(account()));

        mockMvc.perform(get("/account").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountType").value("Savings"));
    }

    @Test
    @WithMockUser(username = "sachin@example.com", roles = "USER")
    @DisplayName("a denial from the service becomes a 403")
    void refusesAnotherCustomersAccount() throws Exception {
        when(accountService.findAccountFor(any(), eq(2L)))
                .thenThrow(new AccessDeniedException("The requested customer id is not available to this user"));

        mockMvc.perform(get("/account").param("id", "2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "sachin@example.com", roles = "USER")
    @DisplayName("a customer with no account row gets 404 rather than an empty 200")
    void returnsNotFoundWhenNoAccountExists() throws Exception {
        when(accountService.findAccountFor(any(), eq(1L))).thenReturn(Optional.empty());

        mockMvc.perform(get("/account").param("id", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("an anonymous caller never reaches the controller")
    void anonymousIsRefused() throws Exception {
        // Boot's default test security chain answers either 302 (form login) or
        // 401 (basic) depending on the request headers, so assert the thing that
        // actually matters rather than a specific status.
        mockMvc.perform(get("/account").param("id", "1"))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(200));

        verifyNoInteractions(accountService);
    }
}
