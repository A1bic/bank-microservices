package ru.kovalenko.bank.infrastructure.rates;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Message published by rates-service to Kafka.
 */
public record ExchangeRateMessage(
        String currency,
        BigDecimal rateToRub,
        Instant timestamp
) {
}
