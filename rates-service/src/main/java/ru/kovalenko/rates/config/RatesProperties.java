package ru.kovalenko.rates.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * @param topic    Kafka topic to publish rates to
 * @param currency currency whose rate to rubles is published
 * @param minRate  lower bound of the simulated rate
 * @param maxRate  upper bound of the simulated rate
 */
@ConfigurationProperties(prefix = "rates")
public record RatesProperties(String topic, String currency, BigDecimal minRate, BigDecimal maxRate) {
    public RatesProperties {
        if (minRate.compareTo(maxRate) >= 0) {
            throw new IllegalArgumentException("rates.min-rate must be less than rates.max-rate");
        }
    }
}
