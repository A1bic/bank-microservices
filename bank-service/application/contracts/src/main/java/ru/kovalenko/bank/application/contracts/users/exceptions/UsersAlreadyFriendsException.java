package ru.kovalenko.bank.application.contracts.users.exceptions;

public class UsersAlreadyFriendsException extends RuntimeException {
    public UsersAlreadyFriendsException(long userId, long friendId) {
        super("Users " + userId + " and " + friendId + " are already friends");
    }
}
