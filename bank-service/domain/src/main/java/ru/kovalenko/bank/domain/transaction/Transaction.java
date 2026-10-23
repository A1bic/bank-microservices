package ru.kovalenko.bank.domain.transaction;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.kovalenko.bank.domain.account.AccountId;
import ru.kovalenko.bank.domain.common.Money;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * A single entry in an account's history.
 * <p>
 * {@code accountId} is the account the entry belongs to, {@code counterpartyAccountId} is the other side
 * of a transfer. For deposits and withdrawals both ids point to the same account.
 */
@Getter
@Setter
@NoArgsConstructor
public class Transaction {
    private TransactionId id;
    private TransactionType transactionType;
    private Money amount;
    private Money commission;
    private Instant createdAt;
    private String description;
    private AccountId accountId;
    private AccountId counterpartyAccountId;

    private Transaction(TransactionType type,
                        Money amount,
                        Money commission,
                        String description,
                        AccountId accountId,
                        AccountId counterpartyAccountId) {
        this.transactionType = type;
        this.amount = amount;
        this.commission = commission;
        this.description = description;
        this.accountId = accountId;
        this.counterpartyAccountId = counterpartyAccountId;
        // PostgreSQL stores timestamps with microsecond precision
        this.createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    public static Transaction deposit(AccountId accountId, Money amount) {
        return new Transaction(TransactionType.DEPOSIT, amount, Money.zero(),
                "Deposit of " + amount, accountId, accountId);
    }

    public static Transaction withdrawal(AccountId accountId, Money amount) {
        return new Transaction(TransactionType.WITHDRAW, amount, Money.zero(),
                "Withdrawal of " + amount, accountId, accountId);
    }

    /**
     * Outgoing part of a transfer. {@code totalDebit} already includes the commission.
     */
    public static Transaction transferOut(AccountId from, AccountId to, Money totalDebit, Money commission) {
        return new Transaction(TransactionType.TRANSFER_OUT, totalDebit, commission,
                "Transfer to account " + to.value() + ", commission = " + commission, from, to);
    }

    /**
     * Incoming part of a transfer. Stored in the history of the receiving account.
     */
    public static Transaction transferIn(AccountId from, AccountId to, Money amount) {
        return new Transaction(TransactionType.TRANSFER_IN, amount, Money.zero(),
                "Transfer from account " + from.value(), to, from);
    }
}
