package ru.kovalenko.bank.application.mapping;

import ru.kovalenko.bank.application.contracts.transactions.models.TransactionDto;
import ru.kovalenko.bank.domain.transaction.Transaction;

public final class TransactionDtoMapper {
    private TransactionDtoMapper() {
    }

    public static TransactionDto toDto(Transaction transaction) {
        return toDto(transaction, CurrencyConversion.none());
    }

    public static TransactionDto toDto(Transaction transaction, CurrencyConversion conversion) {
        return new TransactionDto(
                transaction.getId().value(),
                transaction.getTransactionType().name(),
                conversion.convert(transaction.getAmount()),
                conversion.convert(transaction.getCommission()),
                conversion.currency(),
                transaction.getAccountId().value(),
                transaction.getCounterpartyAccountId().value(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }
}
