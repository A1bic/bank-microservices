package ru.kovalenko.bank.application.contracts.accounts.models;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Bank account")
public record AccountDto(
        @Schema(description = "Account ID", example = "1")
        long id,

        @Schema(description = "Owner (user) ID", example = "2")
        long ownerId,

        @Schema(description = "Balance in rubles", example = "1000.00")
        BigDecimal balance
) {
}
