package com.example.OAuthBankingBackendApplication.dto;

/**
 * Credentials posted to {@code POST /apiLogin}.
 *
 * @param username the customer's e-mail address
 * @param password the plain-text password, checked against the stored hash
 */
public record LoginRequestDTO(String username, String password) {
}
