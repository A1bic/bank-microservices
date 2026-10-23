package ru.kovalenko.bank.application.contracts.users.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(long userId) {
        super("User not found: id = " + userId);
    }
}
