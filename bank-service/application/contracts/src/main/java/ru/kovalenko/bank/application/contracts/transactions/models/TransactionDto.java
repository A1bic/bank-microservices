package ru.kovalenko.bank.application.contracts.transactions.models;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Entry in the account history")
public record TransactionDto(
        @Schema(description = "Transaction ID", example = "1")
        long id,

        @Schema(description = "Transaction type", example = "TRANSFER_OUT",
                allowableValues = {"DEPOSIT", "WITHDRAW", "TRANSFER_OUT", "TRANSFER_IN"})
        String type,

        @Schema(description = "Amount. For TRANSFER_OUT it already includes the commission", example = "110.00")
        BigDecimal amount,

        @Schema(description = "Commission charged for the operation", example = "10.00")
        BigDecimal commission,

        @Schema(description = "Currency of amount and commission", example = "RUB")
        String currency,

        @Schema(description = "Account the transaction belongs to", example = "1")
        long accountId,

        @Schema(description = "Other side of a transfer. Equals accountId for deposits and withdrawals", example = "2")
        long counterpartyAccountId,

        @Schema(description = "Human readable description", example = "Transfer to account 2, commission = 10.00")
        String description,

        @Schema(description = "When the transaction happened (UTC)", example = "2026-04-07T12:30:00Z")
        Instant createdAt
) {
}
