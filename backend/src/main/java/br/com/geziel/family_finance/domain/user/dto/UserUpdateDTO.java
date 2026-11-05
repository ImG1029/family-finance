package br.com.geziel.family_finance.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateDTO(
        @NotBlank String name
) {}
