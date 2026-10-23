package ru.kovalenko.rates.publisher;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Message consumed by bank-service.
 *
 * @param rateToRub how many rubles one unit of {@code currency} costs
 */
public record ExchangeRateMessage(
        String currency,
        BigDecimal rateToRub,
        Instant timestamp
) {
}
