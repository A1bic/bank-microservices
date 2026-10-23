package ru.kovalenko.bank.infrastructure.rates;

import org.springframework.stereotype.Component;
import ru.kovalenko.bank.application.abstractions.rates.ExchangeRateProvider;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps the latest known rate for every currency.
 * Written by the Kafka listener thread and read by request threads, hence the concurrent map.
 */
@Component
public class InMemoryExchangeRateProvider implements ExchangeRateProvider {
    private final Map<String, BigDecimal> ratesToRub = new ConcurrentHashMap<>();

    @Override
    public Optional<BigDecimal> findRateToRub(String currencyCode) {
        return Optional.ofNullable(ratesToRub.get(normalize(currencyCode)));
    }

    public void updateRate(String currencyCode, BigDecimal rateToRub) {
        if (rateToRub.signum() <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive, got " + rateToRub);
        }
        ratesToRub.put(normalize(currencyCode), rateToRub);
    }

    private static String normalize(String currencyCode) {
        return currencyCode.trim().toUpperCase(Locale.ROOT);
    }
}
