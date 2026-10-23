package ru.kovalenko.bank.infrastructure.persistence.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.kovalenko.bank.domain.account.Account;
import ru.kovalenko.bank.infrastructure.persistence.entities.AccountEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountEntityMapper extends CommonMappings {
    @Mapping(target = "ownerId", source = "owner")
    Account toDomain(AccountEntity entity);

    /**
     * Associations are resolved by the repository adapter.
     */
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "transactionHistory", ignore = true)
    AccountEntity toEntity(Account account);
}
