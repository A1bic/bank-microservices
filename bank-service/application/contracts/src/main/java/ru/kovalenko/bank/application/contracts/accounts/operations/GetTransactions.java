package ru.kovalenko.bank.application.contracts.accounts.operations;

import ru.kovalenko.bank.application.contracts.transactions.models.TransactionDto;

import java.util.List;
import java.util.Set;

public final class GetTransactions {
    private GetTransactions() {
    }

    /**
     * @param types    transaction types to include; all types when empty
     * @param currency currency to convert amounts to, e.g. RUB or USD
     */
    public record Request(long accountId, Set<String> types, String currency) {
    }

    public record Response(List<TransactionDto> transactions) {
    }
}
