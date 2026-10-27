package br.com.geziel.family_finance.domain.auth.dto;

public record AccessTokenResponseDTO (
    String accessToken,
    String tokenType,
    Long expiresIn
) {}