package ru.kovalenko.bank.presentation.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull
        @Schema(description = "Account to take money from", example = "1")
        Long fromAccountId,

        @NotNull
        @Schema(description = "Account to send money to", example = "2")
        Long toAccountId,

        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 17, fraction = 2)
        @Schema(description = "Amount the receiver gets, in rubles. The commission is charged on top", example = "100.00")
        BigDecimal amount
) {
}
