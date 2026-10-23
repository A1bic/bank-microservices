package ru.kovalenko.bank.application.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kovalenko.bank.application.abstractions.persistence.RoleRepository;
import ru.kovalenko.bank.application.abstractions.persistence.UserRepository;
import ru.kovalenko.bank.application.common.Enums;
import ru.kovalenko.bank.application.contracts.users.UserService;
import ru.kovalenko.bank.application.contracts.users.exceptions.GenderNotFoundException;
import ru.kovalenko.bank.application.contracts.users.exceptions.RoleNotFoundException;
import ru.kovalenko.bank.application.contracts.users.exceptions.UserAlreadyExistsException;
import ru.kovalenko.bank.application.contracts.users.exceptions.UserNotFoundException;
import ru.kovalenko.bank.application.contracts.users.exceptions.UsersAlreadyFriendsException;
import ru.kovalenko.bank.application.contracts.users.exceptions.UsersNotFriendsException;
import ru.kovalenko.bank.application.contracts.users.operations.AddFriend;
import ru.kovalenko.bank.application.contracts.users.operations.CreateUser;
import ru.kovalenko.bank.application.contracts.users.operations.GetAllUsers;
import ru.kovalenko.bank.application.contracts.users.operations.GetFriends;
import ru.kovalenko.bank.application.contracts.users.operations.GetUserById;
import ru.kovalenko.bank.application.contracts.users.operations.GetUsersByFilter;
import ru.kovalenko.bank.application.contracts.users.operations.RemoveFriend;
import ru.kovalenko.bank.application.mapping.UserDtoMapper;
import ru.kovalenko.bank.domain.role.Role;
import ru.kovalenko.bank.domain.role.RoleName;
import ru.kovalenko.bank.domain.user.Gender;
import ru.kovalenko.bank.domain.user.User;
import ru.kovalenko.bank.domain.user.UserId;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public CreateUser.Response createUser(CreateUser.Request request) {
        if (userRepository.existsByLogin(request.login())) {
            throw new UserAlreadyExistsException(request.login());
        }
        RoleName roleName = Enums.parse(RoleName.class, request.role())
                .orElseThrow(() -> new RoleNotFoundException(request.role()));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException(request.role()));

        User user = new User(
                request.login(),
                request.name(),
                request.age(),
                parseGender(request.gender()),
                request.hairColor(),
                role.getId(),
                passwordEncoder.encode(request.password())
        );
        return new CreateUser.Response(UserDtoMapper.toDto(userRepository.save(user)));
    }

    @Override
    @Transactional(readOnly = true)
    public GetUserById.Response getUserById(GetUserById.Request request) {
        User user = userRepository.findById(new UserId(request.userId()))
                .orElseThrow(() -> new UserNotFoundException(request.userId()));
        return new GetUserById.Response(UserDtoMapper.toDto(user));
    }

    @Override
    @Transactional(readOnly = true)
    public GetAllUsers.Response getAllUsers(GetAllUsers.Request request) {
        var users = userRepository.findAll().stream()
                .map(UserDtoMapper::toDto)
                .toList();
        return new GetAllUsers.Response(users);
    }

    @Override
    @Transactional(readOnly = true)
    public GetUsersByFilter.Response getUsersByFilter(GetUsersByFilter.Request request) {
        Set<Gender> genders = request.genders().stream()
                .map(UserServiceImpl::parseGender)
                .collect(Collectors.toSet());
        var users = userRepository.findByGendersAndHairColors(genders, request.hairColors()).stream()
                .map(UserDtoMapper::toDto)
                .toList();
        return new GetUsersByFilter.Response(users);
    }

    @Override
    @Transactional
    public AddFriend.Response addFriend(AddFriend.Request request) {
        if (request.userId() == request.friendId()) {
            throw new IllegalArgumentException("User cannot add themselves as a friend");
        }
        User user = requireUserWithFriends(request.userId());
        User friend = requireUserWithFriends(request.friendId());
        if (user.isFriendWith(friend.getId())) {
            throw new UsersAlreadyFriendsException(request.userId(), request.friendId());
        }

        user.addFriend(friend.getId());
        friend.addFriend(user.getId());
        userRepository.save(user);
        userRepository.save(friend);
        return new AddFriend.Response();
    }

    @Override
    @Transactional
    public RemoveFriend.Response removeFriend(RemoveFriend.Request request) {
        User user = requireUserWithFriends(request.userId());
        User friend = requireUserWithFriends(request.friendId());
        if (!user.isFriendWith(friend.getId())) {
            throw new UsersNotFriendsException(request.userId(), request.friendId());
        }

        user.removeFriend(friend.getId());
        friend.removeFriend(user.getId());
        userRepository.save(user);
        userRepository.save(friend);
        return new RemoveFriend.Response();
    }

    @Override
    @Transactional(readOnly = true)
    public GetFriends.Response getFriends(GetFriends.Request request) {
        UserId userId = new UserId(request.userId());
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(request.userId());
        }
        var friends = userRepository.findFriendsByUserId(userId).stream()
                .map(UserDtoMapper::toDto)
                .toList();
        return new GetFriends.Response(friends);
    }

    private User requireUserWithFriends(long userId) {
        return userRepository.findWithFriendsById(new UserId(userId))
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private static Gender parseGender(String gender) {
        return Enums.parse(Gender.class, gender)
                .orElseThrow(() -> new GenderNotFoundException(gender));
    }
}
