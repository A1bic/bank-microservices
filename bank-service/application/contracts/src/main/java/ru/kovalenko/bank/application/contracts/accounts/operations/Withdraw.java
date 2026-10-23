package ru.kovalenko.bank.application.contracts.accounts.operations;

import ru.kovalenko.bank.application.contracts.transactions.models.TransactionDto;

import java.math.BigDecimal;

public final class Withdraw {
    private Withdraw() {
    }

    public record Request(long accountId, BigDecimal amount) {
    }

    public record Response(TransactionDto transaction) {
    }
}
