package ru.kovalenko.bank.application.mapping;

import ru.kovalenko.bank.application.contracts.accounts.models.AccountDto;
import ru.kovalenko.bank.domain.account.Account;

public final class AccountDtoMapper {
    private AccountDtoMapper() {
    }

    public static AccountDto toDto(Account account) {
        return new AccountDto(
                account.getId().value(),
                account.getOwnerId().value(),
                account.getBalance().getAmount()
        );
    }
}
