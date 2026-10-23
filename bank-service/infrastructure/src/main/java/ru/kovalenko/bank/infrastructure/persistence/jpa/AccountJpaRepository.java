package ru.kovalenko.bank.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.kovalenko.bank.infrastructure.persistence.entities.AccountEntity;

import java.util.List;
import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long> {
    @EntityGraph(attributePaths = "transactionHistory")
    Optional<AccountEntity> findWithTransactionHistoryById(Long id);

    @EntityGraph(attributePaths = "transactionHistory")
    List<AccountEntity> findAllByOwnerIdOrderById(Long ownerId);
}
