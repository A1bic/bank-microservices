package ru.kovalenko.bank.application.mapping;

import ru.kovalenko.bank.application.contracts.users.models.UserDto;
import ru.kovalenko.bank.domain.user.User;

public final class UserDtoMapper {
    private UserDtoMapper() {
    }

    public static UserDto toDto(User user) {
        return new UserDto(
                user.getId().value(),
                user.getLogin(),
                user.getName(),
                user.getAge(),
                user.getGender().name(),
                user.getHairColor()
        );
    }
}
