package ru.kovalenko.bank.application.contracts.users.operations;

import ru.kovalenko.bank.application.contracts.users.models.UserDto;

import java.util.List;

public final class GetFriends {
    private GetFriends() {
    }

    public record Request(long userId) {
    }

    public record Response(List<UserDto> friends) {
    }
}
