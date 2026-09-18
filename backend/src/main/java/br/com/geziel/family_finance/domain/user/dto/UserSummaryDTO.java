package br.com.geziel.family_finance.domain.user.dto;

import java.util.UUID;

public record UserSummaryDTO(
        UUID id,
        String name,
        String email
) {}
