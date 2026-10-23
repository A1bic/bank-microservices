package ru.kovalenko.bank.presentation.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotNull
        @PositiveOrZero
        @Digits(integer = 17, fraction = 2)
        @Schema(description = "Initial balance in rubles", example = "1000.00")
        BigDecimal initialBalance
) {
}
