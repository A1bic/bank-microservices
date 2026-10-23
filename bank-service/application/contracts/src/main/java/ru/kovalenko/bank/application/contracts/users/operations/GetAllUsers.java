package ru.kovalenko.bank.application.contracts.users.operations;

import ru.kovalenko.bank.application.contracts.users.models.UserDto;

import java.util.List;

public final class GetAllUsers {
    private GetAllUsers() {
    }

    public record Request() {
    }

    public record Response(List<UserDto> users) {
    }
}
