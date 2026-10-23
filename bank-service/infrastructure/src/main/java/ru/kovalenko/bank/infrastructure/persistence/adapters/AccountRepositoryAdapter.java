package ru.kovalenko.bank.infrastructure.persistence.adapters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.kovalenko.bank.application.abstractions.persistence.AccountRepository;
import ru.kovalenko.bank.domain.account.Account;
import ru.kovalenko.bank.domain.account.AccountId;
import ru.kovalenko.bank.domain.transaction.TransactionId;
import ru.kovalenko.bank.domain.user.UserId;
import ru.kovalenko.bank.infrastructure.persistence.entities.AccountEntity;
import ru.kovalenko.bank.infrastructure.persistence.jpa.AccountJpaRepository;
import ru.kovalenko.bank.infrastructure.persistence.jpa.TransactionJpaRepository;
import ru.kovalenko.bank.infrastructure.persistence.jpa.UserJpaRepository;
import ru.kovalenko.bank.infrastructure.persistence.mappers.AccountEntityMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {
    private final AccountJpaRepository accountJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final TransactionJpaRepository transactionJpaRepository;
    private final AccountEntityMapper mapper;

    @Override
    public Account save(Account account) {
        AccountEntity entity = mapper.toEntity(account);
        entity.setOwner(userJpaRepository.getReferenceById(account.getOwnerId().value()));
        // The history is owned by TransactionEntity; we only keep the in-memory collection in sync
        entity.setTransactionHistory(account.getTransactionHistory().stream()
                .map(TransactionId::value)
                .map(transactionJpaRepository::getReferenceById)
                .collect(Collectors.toCollection(ArrayList::new)));
        return mapper.toDomain(accountJpaRepository.save(entity));
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        return accountJpaRepository.findWithTransactionHistoryById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<Account> findByOwnerId(UserId ownerId) {
        return accountJpaRepository.findAllByOwnerIdOrderById(ownerId.value()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(AccountId id) {
        return accountJpaRepository.existsById(id.value());
    }
}
