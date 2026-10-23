package ru.kovalenko.bank.infrastructure.persistence.adapters;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import ru.kovalenko.bank.application.abstractions.persistence.UserRepository;
import ru.kovalenko.bank.domain.role.RoleName;
import ru.kovalenko.bank.domain.user.Gender;
import ru.kovalenko.bank.domain.user.User;
import ru.kovalenko.bank.domain.user.UserId;
import ru.kovalenko.bank.infrastructure.persistence.entities.UserEntity;
import ru.kovalenko.bank.infrastructure.persistence.jpa.RoleJpaRepository;
import ru.kovalenko.bank.infrastructure.persistence.jpa.UserJpaRepository;
import ru.kovalenko.bank.infrastructure.persistence.mappers.UserEntityMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository userJpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final UserEntityMapper mapper;

    /**
     * Saves the user together with the friend list, so pass a user loaded with friends
     * ({@link #findWithFriendsById}) when updating an existing one.
     */
    @Override
    public User save(User user) {
        UserEntity entity = mapper.toEntity(user);
        entity.setRole(roleJpaRepository.getReferenceById(user.getRole().value()));
        entity.setFriends(user.getFriends().stream()
                .map(UserId::value)
                .map(userJpaRepository::getReferenceById)
                .collect(Collectors.toCollection(ArrayList::new)));
        return mapper.toDomain(userJpaRepository.save(entity));
    }

    @Override
    public Optional<User> findById(UserId id) {
        return userJpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findWithFriendsById(UserId id) {
        return userJpaRepository.findWithFriendsById(id.value()).map(mapper::toDomainWithFriends);
    }

    @Override
    public Optional<User> findByLogin(String login) {
        return userJpaRepository.findByLogin(login).map(mapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll(Sort.by("id")).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<User> findFriendsByUserId(UserId id) {
        return userJpaRepository.findFriendsByUserId(id.value()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<User> findByGendersAndHairColors(Collection<Gender> genders, Collection<String> hairColors) {
        return userJpaRepository.findAllByGenderInAndHairColorIn(genders, hairColors).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(UserId id) {
        return userJpaRepository.existsById(id.value());
    }

    @Override
    public boolean existsByLogin(String login) {
        return userJpaRepository.existsByLogin(login);
    }

    @Override
    public boolean existsByRole(RoleName role) {
        return userJpaRepository.existsByRoleName(role);
    }
}
