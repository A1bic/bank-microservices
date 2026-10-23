package ru.kovalenko.bank.infrastructure.persistence.adapters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.kovalenko.bank.application.abstractions.persistence.RoleRepository;
import ru.kovalenko.bank.domain.role.Role;
import ru.kovalenko.bank.domain.role.RoleId;
import ru.kovalenko.bank.domain.role.RoleName;
import ru.kovalenko.bank.infrastructure.persistence.jpa.RoleJpaRepository;
import ru.kovalenko.bank.infrastructure.persistence.mappers.RoleEntityMapper;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepository {
    private final RoleJpaRepository roleJpaRepository;
    private final RoleEntityMapper mapper;

    @Override
    public Optional<Role> findById(RoleId id) {
        return roleJpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Role> findByName(RoleName name) {
        return roleJpaRepository.findByName(name).map(mapper::toDomain);
    }
}
