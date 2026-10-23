package ru.kovalenko.bank.domain.user;

import lombok.Getter;
import lombok.Setter;
import ru.kovalenko.bank.domain.account.AccountId;
import ru.kovalenko.bank.domain.role.RoleId;

import java.util.ArrayList;
import java.util.List;

@Getter
public class User {
    @Setter
    private UserId id;

    private final String login;
    private final String name;
    private final int age;
    private final Gender gender;
    private final String hairColor;
    private final RoleId role;
    private final String passwordHash;

    private final List<UserId> friends = new ArrayList<>();
    private final List<AccountId> accounts = new ArrayList<>();

    public User(String login,
                String name,
                int age,
                Gender gender,
                String hairColor,
                RoleId role,
                String passwordHash) {
        this.login = login;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.hairColor = hairColor;
        this.role = role;
        this.passwordHash = passwordHash;
    }

    public boolean isFriendWith(UserId userId) {
        return friends.contains(userId);
    }

    public void addFriend(UserId userId) {
        if (!isFriendWith(userId)) {
            friends.add(userId);
        }
    }

    public void removeFriend(UserId userId) {
        friends.remove(userId);
    }
}
