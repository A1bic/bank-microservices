package ru.kovalenko.bank.infrastructure.persistence.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.kovalenko.bank.domain.transaction.Transaction;
import ru.kovalenko.bank.infrastructure.persistence.entities.TransactionEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionEntityMapper extends CommonMappings {
    @Mapping(target = "accountId", source = "account")
    @Mapping(target = "counterpartyAccountId", source = "counterpartyAccount")
    Transaction toDomain(TransactionEntity entity);

    /**
     * Associations are resolved by the repository adapter.
     */
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "counterpartyAccount", ignore = true)
    TransactionEntity toEntity(Transaction transaction);
}
