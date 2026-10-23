package ru.kovalenko.bank.application.contracts.users.models;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Bank user")
public record UserDto(
        @Schema(description = "User ID", example = "1")
        long id,

        @Schema(description = "Login", example = "vasya")
        String login,

        @Schema(description = "Name", example = "Vasya")
        String name,

        @Schema(description = "Age", example = "25")
        int age,

        @Schema(description = "Gender", example = "MALE")
        String gender,

        @Schema(description = "Hair color", example = "black")
        String hairColor
) {
}
