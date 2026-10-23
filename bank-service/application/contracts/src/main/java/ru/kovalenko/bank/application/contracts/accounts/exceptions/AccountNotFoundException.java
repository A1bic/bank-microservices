package ru.kovalenko.bank.application.contracts.accounts.exceptions;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(long accountId) {
        super("Account not found: id = " + accountId);
    }
}
