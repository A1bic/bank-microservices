package ru.kovalenko.rates.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.kovalenko.rates.config.RatesProperties;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulates an exchange rate feed: publishes a random rate within the configured range on a fixed schedule.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRatePublisher {
    private final KafkaTemplate<String, ExchangeRateMessage> kafkaTemplate;
    private final RatesProperties properties;

    @Scheduled(initialDelayString = "${rates.initial-delay}", fixedRateString = "${rates.publish-interval}")
    public void publishRate() {
        var message = new ExchangeRateMessage(properties.currency(), nextRate(), Instant.now());
        kafkaTemplate.send(properties.topic(), message.currency(), message);
        log.info("Published rate: 1 {} = {} RUB", message.currency(), message.rateToRub());
    }

    private BigDecimal nextRate() {
        double rate = ThreadLocalRandom.current().nextDouble(
                properties.minRate().doubleValue(),
                properties.maxRate().doubleValue());
        return BigDecimal.valueOf(rate).setScale(4, RoundingMode.HALF_UP);
    }
}
