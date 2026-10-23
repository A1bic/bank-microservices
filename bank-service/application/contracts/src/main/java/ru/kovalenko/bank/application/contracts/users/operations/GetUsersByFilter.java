package ru.kovalenko.bank.application.contracts.users.operations;

import ru.kovalenko.bank.application.contracts.users.models.UserDto;

import java.util.List;
import java.util.Set;

public final class GetUsersByFilter {
    private GetUsersByFilter() {
    }

    public record Request(Set<String> genders, Set<String> hairColors) {
    }

    public record Response(List<UserDto> users) {
    }
}
