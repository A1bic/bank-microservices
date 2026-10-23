package ru.kovalenko.bank.application.contracts.accounts.exceptions;

public class CurrencyNotFoundException extends RuntimeException {
    public CurrencyNotFoundException(String currency) {
        super("Exchange rate is not available for currency: " + currency);
    }
}
