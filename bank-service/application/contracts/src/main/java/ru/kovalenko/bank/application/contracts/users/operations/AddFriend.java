package ru.kovalenko.bank.application.contracts.users.operations;

public final class AddFriend {
    private AddFriend() {
    }

    public record Request(long userId, long friendId) {
    }

    public record Response() {
    }
}
