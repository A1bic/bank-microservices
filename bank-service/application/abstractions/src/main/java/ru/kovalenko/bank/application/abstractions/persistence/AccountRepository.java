package ru.kovalenko.bank.application.abstractions.persistence;

import ru.kovalenko.bank.domain.account.Account;
import ru.kovalenko.bank.domain.account.AccountId;
import ru.kovalenko.bank.domain.user.UserId;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);

    Optional<Account> findById(AccountId id);

    List<Account> findByOwnerId(UserId ownerId);

    boolean existsById(AccountId id);
}
