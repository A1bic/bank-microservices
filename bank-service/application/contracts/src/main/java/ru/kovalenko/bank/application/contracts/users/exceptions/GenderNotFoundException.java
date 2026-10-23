package ru.kovalenko.bank.application.contracts.users.exceptions;

public class GenderNotFoundException extends RuntimeException {
    public GenderNotFoundException(String gender) {
        super("Unknown gender: " + gender + ". Allowed values: MALE, FEMALE");
    }
}
