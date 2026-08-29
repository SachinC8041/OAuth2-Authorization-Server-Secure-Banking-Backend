package com.example.OAuthBankingBackendApplication.dto;

/**
 * Result of a successful call to {@code POST /apiLogin}.
 *
 * <p>The same token is also returned in the {@code Authorization} response header,
 * which is what the browser client reads.
 *
 * @param status   HTTP reason phrase for the outcome
 * @param jwtToken the signed JWT to send on subsequent requests
 */
public record LoginResponseDTO(String status, String jwtToken) {
}
