package ru.kovalenko.bank.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.kovalenko.bank.domain.role.RoleName;
import ru.kovalenko.bank.domain.user.Gender;
import ru.kovalenko.bank.infrastructure.persistence.entities.UserEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    @EntityGraph(attributePaths = "friends")
    Optional<UserEntity> findWithFriendsById(Long id);

    Optional<UserEntity> findByLogin(String login);

    List<UserEntity> findAllByGenderInAndHairColorIn(Collection<Gender> genders, Collection<String> hairColors);

    @Query("select f from UserEntity u join u.friends f where u.id = :userId order by f.id")
    List<UserEntity> findFriendsByUserId(Long userId);

    boolean existsByLogin(String login);

    boolean existsByRoleName(RoleName roleName);
}
