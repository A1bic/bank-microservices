package ru.kovalenko.bank.application.contracts.accounts.operations;

import ru.kovalenko.bank.application.contracts.accounts.models.AccountDto;

public final class GetAccountById {
    private GetAccountById() {
    }

    public record Request(long accountId) {
    }

    public record Response(AccountDto account) {
    }
}
