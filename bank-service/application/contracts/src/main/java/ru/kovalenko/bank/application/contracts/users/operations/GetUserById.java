package ru.kovalenko.bank.application.contracts.users.operations;

import ru.kovalenko.bank.application.contracts.users.models.UserDto;

public final class GetUserById {
    private GetUserById() {
    }

    public record Request(long userId) {
    }

    public record Response(UserDto user) {
    }
}
