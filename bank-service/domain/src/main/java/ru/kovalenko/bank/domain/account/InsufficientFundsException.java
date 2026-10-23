package ru.kovalenko.bank.domain.account;

import ru.kovalenko.bank.domain.common.Money;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(Money balance, Money required) {
        super("Insufficient funds: balance = " + balance + ", required = " + required);
    }
}
