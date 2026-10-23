package ru.kovalenko.bank.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.kovalenko.bank.domain.transaction.TransactionType;
import ru.kovalenko.bank.infrastructure.persistence.entities.TransactionEntity;

import java.util.Collection;
import java.util.List;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, Long> {
    @Query("""
            select t from TransactionEntity t
            where t.account.id = :accountId and t.transactionType in :types
            order by t.createdAt desc, t.id desc
            """)
    List<TransactionEntity> findHistory(Long accountId, Collection<TransactionType> types);
}
