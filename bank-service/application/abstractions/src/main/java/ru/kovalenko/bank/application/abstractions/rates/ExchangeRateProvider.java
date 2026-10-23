package ru.kovalenko.bank.application.abstractions.rates;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Source of current exchange rates.
 */
public interface ExchangeRateProvider {
    /**
     * @param currencyCode ISO currency code, e.g. {@code USD}
     * @return how many rubles one unit of the currency costs, or empty if the rate is unknown
     */
    Optional<BigDecimal> findRateToRub(String currencyCode);
}
