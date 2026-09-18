package br.com.geziel.family_finance.domain.auth.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RefreshRequestDTO(
        @NotNull UUID refreshToken
) {}
