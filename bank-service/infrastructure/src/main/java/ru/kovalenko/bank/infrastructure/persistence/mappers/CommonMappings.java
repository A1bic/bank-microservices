package ru.kovalenko.bank.infrastructure.persistence.mappers;

import ru.kovalenko.bank.domain.account.AccountId;
import ru.kovalenko.bank.domain.common.Money;
import ru.kovalenko.bank.domain.role.RoleId;
import ru.kovalenko.bank.domain.transaction.TransactionId;
import ru.kovalenko.bank.domain.user.UserId;
import ru.kovalenko.bank.infrastructure.persistence.entities.AccountEntity;
import ru.kovalenko.bank.infrastructure.persistence.entities.RoleEntity;
import ru.kovalenko.bank.infrastructure.persistence.entities.TransactionEntity;
import ru.kovalenko.bank.infrastructure.persistence.entities.UserEntity;

import java.math.BigDecimal;

/**
 * Conversions between domain value objects and database types, shared by all entity mappers.
 * MapStruct picks these methods automatically by their parameter and return types.
 */
interface CommonMappings {
    default Money toMoney(BigDecimal amount) {
        return amount == null ? null : Money.of(amount);
    }

    default BigDecimal toAmount(Money money) {
        return money == null ? null : money.getAmount();
    }

    default AccountId toAccountId(Long id) {
        return id == null ? null : new AccountId(id);
    }

    default Long fromAccountId(AccountId id) {
        return id == null ? null : id.value();
    }

    default UserId toUserId(Long id) {
        return id == null ? null : new UserId(id);
    }

    default Long fromUserId(UserId id) {
        return id == null ? null : id.value();
    }

    default TransactionId toTransactionId(Long id) {
        return id == null ? null : new TransactionId(id);
    }

    default Long fromTransactionId(TransactionId id) {
        return id == null ? null : id.value();
    }

    default RoleId toRoleId(Long id) {
        return id == null ? null : new RoleId(id);
    }

    default Long fromRoleId(RoleId id) {
        return id == null ? null : id.value();
    }

    // Associations are mapped to ids only. Reading the id of a lazy proxy does not hit the database.

    default AccountId toAccountId(AccountEntity entity) {
        return entity == null ? null : new AccountId(entity.getId());
    }

    default UserId toUserId(UserEntity entity) {
        return entity == null ? null : new UserId(entity.getId());
    }

    default TransactionId toTransactionId(TransactionEntity entity) {
        return entity == null ? null : new TransactionId(entity.getId());
    }

    default RoleId toRoleId(RoleEntity entity) {
        return entity == null ? null : new RoleId(entity.getId());
    }
}
