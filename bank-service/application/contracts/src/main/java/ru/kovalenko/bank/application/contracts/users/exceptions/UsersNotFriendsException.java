package ru.kovalenko.bank.application.contracts.users.exceptions;

public class UsersNotFriendsException extends RuntimeException {
    public UsersNotFriendsException(long userId, long friendId) {
        super("Users " + userId + " and " + friendId + " are not friends");
    }
}
