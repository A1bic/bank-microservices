package ru.kovalenko.bank.application.contracts.users.operations;

import ru.kovalenko.bank.application.contracts.users.models.UserDto;

public final class CreateUser {
    private CreateUser() {
    }

    public record Request(String login,
                          String password,
                          String name,
                          int age,
                          String gender,
                          String hairColor,
                          String role) {
    }

    public record Response(UserDto user) {
    }
}
