package ru.kovalenko.bank.application.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kovalenko.bank.application.abstractions.persistence.RoleRepository;
import ru.kovalenko.bank.application.abstractions.persistence.UserRepository;
import ru.kovalenko.bank.application.contracts.users.exceptions.GenderNotFoundException;
import ru.kovalenko.bank.application.contracts.users.exceptions.RoleNotFoundException;
import ru.kovalenko.bank.application.contracts.users.exceptions.UserAlreadyExistsException;
import ru.kovalenko.bank.application.contracts.users.exceptions.UsersAlreadyFriendsException;
import ru.kovalenko.bank.application.contracts.users.exceptions.UsersNotFriendsException;
import ru.kovalenko.bank.application.contracts.users.operations.AddFriend;
import ru.kovalenko.bank.application.contracts.users.operations.CreateUser;
import ru.kovalenko.bank.application.contracts.users.operations.RemoveFriend;
import ru.kovalenko.bank.domain.role.Role;
import ru.kovalenko.bank.domain.role.RoleId;
import ru.kovalenko.bank.domain.role.RoleName;
import ru.kovalenko.bank.domain.user.Gender;
import ru.kovalenko.bank.domain.user.User;
import ru.kovalenko.bank.domain.user.UserId;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_storesHashedPassword() {
        var clientRole = new Role(RoleName.CLIENT);
        clientRole.setId(new RoleId(2L));
        when(userRepository.existsByLogin("vasya")).thenReturn(false);
        when(roleRepository.findByName(RoleName.CLIENT)).thenReturn(Optional.of(clientRole));
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(new UserId(5L));
            return user;
        });

        var response = userService.createUser(createRequest("vasya", "male", "client"));

        var saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.getValue().getGender()).isEqualTo(Gender.MALE);
        assertThat(saved.getValue().getRole()).isEqualTo(new RoleId(2L));
        assertThat(response.user().id()).isEqualTo(5L);
        assertThat(response.user().login()).isEqualTo("vasya");
    }

    @Test
    void createUser_takenLogin_isRejected() {
        when(userRepository.existsByLogin("vasya")).thenReturn(true);

        var request = createRequest("vasya", "MALE", "CLIENT");

        assertThatThrownBy(() -> userService.createUser(request)).isInstanceOf(UserAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_unknownRole_isRejected() {
        when(userRepository.existsByLogin("vasya")).thenReturn(false);

        var request = createRequest("vasya", "MALE", "SUPERUSER");

        assertThatThrownBy(() -> userService.createUser(request)).isInstanceOf(RoleNotFoundException.class);
    }

    @Test
    void createUser_unknownGender_isRejected() {
        var clientRole = new Role(RoleName.CLIENT);
        clientRole.setId(new RoleId(2L));
        when(userRepository.existsByLogin("vasya")).thenReturn(false);
        when(roleRepository.findByName(RoleName.CLIENT)).thenReturn(Optional.of(clientRole));

        var request = createRequest("vasya", "UNKNOWN", "CLIENT");

        assertThatThrownBy(() -> userService.createUser(request)).isInstanceOf(GenderNotFoundException.class);
    }

    @Test
    void addFriend_makesFriendshipMutual() {
        User alice = user(1L);
        User bob = user(2L);
        givenUsersWithFriends(alice, bob);

        userService.addFriend(new AddFriend.Request(1L, 2L));

        assertThat(alice.isFriendWith(bob.getId())).isTrue();
        assertThat(bob.isFriendWith(alice.getId())).isTrue();
        verify(userRepository).save(alice);
        verify(userRepository).save(bob);
    }

    @Test
    void addFriend_alreadyFriends_isRejected() {
        User alice = user(1L);
        User bob = user(2L);
        alice.addFriend(bob.getId());
        bob.addFriend(alice.getId());
        givenUsersWithFriends(alice, bob);

        var request = new AddFriend.Request(1L, 2L);

        assertThatThrownBy(() -> userService.addFriend(request)).isInstanceOf(UsersAlreadyFriendsException.class);
    }

    @Test
    void addFriend_self_isRejected() {
        var request = new AddFriend.Request(1L, 1L);

        assertThatThrownBy(() -> userService.addFriend(request)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void removeFriend_removesFriendshipOnBothSides() {
        User alice = user(1L);
        User bob = user(2L);
        alice.addFriend(bob.getId());
        bob.addFriend(alice.getId());
        givenUsersWithFriends(alice, bob);

        userService.removeFriend(new RemoveFriend.Request(1L, 2L));

        assertThat(alice.getFriends()).isEmpty();
        assertThat(bob.getFriends()).isEmpty();
    }

    @Test
    void removeFriend_notFriends_isRejected() {
        givenUsersWithFriends(user(1L), user(2L));

        var request = new RemoveFriend.Request(1L, 2L);

        assertThatThrownBy(() -> userService.removeFriend(request)).isInstanceOf(UsersNotFriendsException.class);
    }

    private static CreateUser.Request createRequest(String login, String gender, String role) {
        return new CreateUser.Request(login, "secret123", "Vasya", 25, gender, "black", role);
    }

    private static User user(long id) {
        var user = new User("user" + id, "name", 20, Gender.FEMALE, "red", new RoleId(2L), "hash");
        user.setId(new UserId(id));
        return user;
    }

    private void givenUsersWithFriends(User... users) {
        for (User user : users) {
            when(userRepository.findWithFriendsById(user.getId())).thenReturn(Optional.of(user));
        }
    }
}
