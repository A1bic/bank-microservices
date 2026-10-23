package ru.kovalenko.bank.application.contracts.accounts.operations;

import ru.kovalenko.bank.application.contracts.accounts.models.AccountDto;

import java.math.BigDecimal;

public final class CreateAccount {
    private CreateAccount() {
    }

    public record Request(long userId, BigDecimal initialBalance) {
    }

    public record Response(AccountDto account) {
    }
}
