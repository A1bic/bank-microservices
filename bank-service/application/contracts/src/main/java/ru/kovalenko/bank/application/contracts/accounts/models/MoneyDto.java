package ru.kovalenko.bank.application.contracts.accounts.models;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Amount of money in the given currency")
public record MoneyDto(
        @Schema(description = "Amount", example = "1000.00")
        BigDecimal amount,

        @Schema(description = "Currency code", example = "RUB")
        String currency
) {
}
