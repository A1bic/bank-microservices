package ru.kovalenko.bank.application.abstractions.persistence;

import ru.kovalenko.bank.domain.account.AccountId;
import ru.kovalenko.bank.domain.transaction.Transaction;
import ru.kovalenko.bank.domain.transaction.TransactionType;

import java.util.Collection;
import java.util.List;

public interface TransactionRepository {
    Transaction save(Transaction transaction);

    /**
     * Returns the account history filtered by transaction types, newest first.
     */
    List<Transaction> findByAccountIdAndTypes(AccountId accountId, Collection<TransactionType> types);
}
