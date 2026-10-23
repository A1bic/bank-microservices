package ru.kovalenko.bank.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kovalenko.bank.domain.role.RoleName;
import ru.kovalenko.bank.infrastructure.persistence.entities.RoleEntity;

import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(RoleName name);
}
