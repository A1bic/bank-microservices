package ru.kovalenko.bank.application.abstractions.persistence;

import ru.kovalenko.bank.domain.role.Role;
import ru.kovalenko.bank.domain.role.RoleId;
import ru.kovalenko.bank.domain.role.RoleName;

import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findById(RoleId id);

    Optional<Role> findByName(RoleName name);
}
