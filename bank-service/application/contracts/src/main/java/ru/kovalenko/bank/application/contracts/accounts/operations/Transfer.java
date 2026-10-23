package ru.kovalenko.bank.application.contracts.accounts.operations;

import ru.kovalenko.bank.application.contracts.transactions.models.TransactionDto;

import java.math.BigDecimal;

public final class Transfer {
    private Transfer() {
    }

    public record Request(long fromAccountId, long toAccountId, BigDecimal amount) {
    }

    /**
     * @param transaction outgoing transaction of the sender (amount includes the commission)
     */
    public record Response(TransactionDto transaction) {
    }
}
