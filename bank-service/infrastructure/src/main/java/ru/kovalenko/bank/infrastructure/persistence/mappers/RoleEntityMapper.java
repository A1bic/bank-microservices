package ru.kovalenko.bank.infrastructure.persistence.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.kovalenko.bank.domain.role.Role;
import ru.kovalenko.bank.infrastructure.persistence.entities.RoleEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoleEntityMapper extends CommonMappings {
    Role toDomain(RoleEntity entity);
}
