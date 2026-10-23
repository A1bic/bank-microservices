package ru.kovalenko.bank.application.contracts.accounts.operations;

import ru.kovalenko.bank.application.contracts.accounts.models.AccountDto;

import java.util.List;

public final class GetAccountsByUserId {
    private GetAccountsByUserId() {
    }

    public record Request(long userId) {
    }

    public record Response(List<AccountDto> accounts) {
    }
}
