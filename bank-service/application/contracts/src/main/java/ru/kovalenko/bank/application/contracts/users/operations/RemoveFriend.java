package ru.kovalenko.bank.application.contracts.users.operations;

public final class RemoveFriend {
    private RemoveFriend() {
    }

    public record Request(long userId, long friendId) {
    }

    public record Response() {
    }
}
