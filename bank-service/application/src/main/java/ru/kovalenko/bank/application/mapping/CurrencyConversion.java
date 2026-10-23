package ru.kovalenko.bank.application.mapping;

import ru.kovalenko.bank.domain.common.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Converts ruble amounts to another currency.
 *
 * @param currency  target currency code
 * @param rateToRub how many rubles one unit of the target currency costs
 */
public record CurrencyConversion(String currency, BigDecimal rateToRub) {
    public static final String BASE_CURRENCY = "RUB";

    public static CurrencyConversion none() {
        return new CurrencyConversion(BASE_CURRENCY, BigDecimal.ONE);
    }

    public BigDecimal convert(Money money) {
        if (BASE_CURRENCY.equals(currency)) {
            return money.getAmount();
        }
        return money.getAmount().divide(rateToRub, 2, RoundingMode.HALF_UP);
    }
}
