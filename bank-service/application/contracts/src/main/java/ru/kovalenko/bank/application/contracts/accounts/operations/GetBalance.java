package ru.kovalenko.bank.application.contracts.accounts.operations;

import ru.kovalenko.bank.application.contracts.accounts.models.MoneyDto;

public final class GetBalance {
    private GetBalance() {
    }

    /**
     * @param currency currency to convert the balance to, e.g. RUB or USD
     */
    public record Request(long accountId, String currency) {
    }

    public record Response(MoneyDto balance) {
    }
}
