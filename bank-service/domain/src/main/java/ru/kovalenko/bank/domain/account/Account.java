package ru.kovalenko.bank.domain.account;

import lombok.Getter;
import lombok.Setter;
import ru.kovalenko.bank.domain.common.Money;
import ru.kovalenko.bank.domain.transaction.TransactionId;
import ru.kovalenko.bank.domain.user.UserId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Bank account. Balance can only be changed through {@link #deposit} and {@link #withdraw},
 * so it can never become negative.
 */
@Getter
public class Account {
    @Setter
    private AccountId id;

    private final UserId ownerId;

    private Money balance;

    private final List<TransactionId> transactionHistory = new ArrayList<>();

    public Account(UserId ownerId, Money balance) {
        this.ownerId = Objects.requireNonNull(ownerId, "ownerId must not be null");
        this.balance = Objects.requireNonNull(balance, "balance must not be null");
        if (balance.isNegative()) {
            throw new IllegalArgumentException("Initial balance must not be negative, got " + balance);
        }
    }

    public void deposit(Money amount) {
        requirePositive(amount);
        balance = balance.add(amount);
    }

    public void withdraw(Money amount) {
        requirePositive(amount);
        if (balance.isLessThan(amount)) {
            throw new InsufficientFundsException(balance, amount);
        }
        balance = balance.subtract(amount);
    }

    public void recordTransaction(TransactionId transactionId) {
        transactionHistory.add(transactionId);
    }

    public boolean belongsTo(UserId userId) {
        return ownerId.equals(userId);
    }

    private static void requirePositive(Money amount) {
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("Amount must be positive, got " + amount);
        }
    }
}
