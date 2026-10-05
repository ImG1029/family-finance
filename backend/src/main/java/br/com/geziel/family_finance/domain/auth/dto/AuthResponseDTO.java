package br.com.geziel.family_finance.domain.auth.dto;

import br.com.geziel.family_finance.domain.user.dto.UserSummaryDTO;

public record AuthResponseDTO(
        String accessToken,
        String refreshToken,
        String tokenType,
        UserSummaryDTO user,
        long expiresInSeconds
) {}
