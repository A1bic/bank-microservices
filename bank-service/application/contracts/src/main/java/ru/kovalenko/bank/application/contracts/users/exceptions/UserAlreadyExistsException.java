package ru.kovalenko.bank.application.contracts.users.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String login) {
        super("User with login '" + login + "' already exists");
    }
}
