package ru.kovalenko.bank.application.contracts.users.exceptions;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String role) {
        super("Unknown role: " + role + ". Allowed values: ADMIN, CLIENT");
    }
}
