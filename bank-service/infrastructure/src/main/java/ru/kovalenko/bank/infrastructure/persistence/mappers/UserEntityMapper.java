package ru.kovalenko.bank.infrastructure.persistence.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.kovalenko.bank.domain.user.User;
import ru.kovalenko.bank.infrastructure.persistence.entities.UserEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserEntityMapper extends CommonMappings {
    /**
     * Maps the user without friends and accounts, so no extra queries are made.
     */
    @Mapping(target = "friends", ignore = true)
    @Mapping(target = "accounts", ignore = true)
    User toDomain(UserEntity entity);

    /**
     * Maps the user together with the friend list.
     */
    @Mapping(target = "accounts", ignore = true)
    User toDomainWithFriends(UserEntity entity);

    /**
     * Associations are resolved by the repository adapter.
     */
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "friends", ignore = true)
    @Mapping(target = "accounts", ignore = true)
    UserEntity toEntity(User user);
}
