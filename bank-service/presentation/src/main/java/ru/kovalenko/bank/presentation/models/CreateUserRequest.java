package ru.kovalenko.bank.presentation.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank
        @Size(max = 64)
        @Schema(example = "vasya")
        String login,

        @NotBlank
        @Size(min = 5, max = 72)
        @Schema(example = "secret123")
        String password,

        @NotBlank
        @Size(max = 255)
        @Schema(example = "Vasya")
        String name,

        @NotNull
        @Min(0)
        @Max(150)
        @Schema(example = "25")
        Integer age,

        @NotBlank
        @Schema(example = "MALE", allowableValues = {"MALE", "FEMALE"})
        String gender,

        @NotBlank
        @Size(max = 64)
        @Schema(example = "black")
        String hairColor,

        @NotBlank
        @Schema(example = "CLIENT", allowableValues = {"ADMIN", "CLIENT"})
        String role
) {
}
