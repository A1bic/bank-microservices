package ru.kovalenko.bank.presentation.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AmountRequest(
        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 17, fraction = 2)
        @Schema(description = "Amount in rubles", example = "300.00")
        BigDecimal amount
) {
}
