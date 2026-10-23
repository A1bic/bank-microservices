package ru.kovalenko.bank.presentation.models;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record CurrentUserResponse(
        @Schema(example = "admin")
        String login,

        @Schema(example = "[\"ADMIN\"]")
        List<String> roles
) {
}
