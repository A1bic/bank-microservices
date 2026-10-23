package ru.kovalenko.bank.application.abstractions.persistence;

import ru.kovalenko.bank.domain.role.RoleName;
import ru.kovalenko.bank.domain.user.Gender;
import ru.kovalenko.bank.domain.user.User;
import ru.kovalenko.bank.domain.user.UserId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(UserId id);

    /**
     * Same as {@link #findById(UserId)}, but also loads the friend list.
     */
    Optional<User> findWithFriendsById(UserId id);

    Optional<User> findByLogin(String login);

    List<User> findAll();

    List<User> findFriendsByUserId(UserId id);

    List<User> findByGendersAndHairColors(Collection<Gender> genders, Collection<String> hairColors);

    boolean existsById(UserId id);

    boolean existsByLogin(String login);

    boolean existsByRole(RoleName role);
}
