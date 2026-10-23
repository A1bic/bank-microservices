package ru.kovalenko.bank.infrastructure.persistence.adapters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.kovalenko.bank.application.abstractions.persistence.TransactionRepository;
import ru.kovalenko.bank.domain.account.AccountId;
import ru.kovalenko.bank.domain.transaction.Transaction;
import ru.kovalenko.bank.domain.transaction.TransactionType;
import ru.kovalenko.bank.infrastructure.persistence.entities.TransactionEntity;
import ru.kovalenko.bank.infrastructure.persistence.jpa.AccountJpaRepository;
import ru.kovalenko.bank.infrastructure.persistence.jpa.TransactionJpaRepository;
import ru.kovalenko.bank.infrastructure.persistence.mappers.TransactionEntityMapper;

import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepository {
    private final TransactionJpaRepository transactionJpaRepository;
    private final AccountJpaRepository accountJpaRepository;
    private final TransactionEntityMapper mapper;

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = mapper.toEntity(transaction);
        entity.setAccount(accountJpaRepository.getReferenceById(transaction.getAccountId().value()));
        entity.setCounterpartyAccount(
                accountJpaRepository.getReferenceById(transaction.getCounterpartyAccountId().value()));
        return mapper.toDomain(transactionJpaRepository.save(entity));
    }

    @Override
    public List<Transaction> findByAccountIdAndTypes(AccountId accountId, Collection<TransactionType> types) {
        return transactionJpaRepository.findHistory(accountId.value(), types).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
