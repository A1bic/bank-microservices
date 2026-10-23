package ru.kovalenko.bank.infrastructure.rates;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateListener {
    private final InMemoryExchangeRateProvider rateProvider;

    @KafkaListener(topics = "${bank.kafka.rates-topic}")
    public void onExchangeRate(ExchangeRateMessage message) {
        if (message.currency() == null || message.rateToRub() == null || message.rateToRub().signum() <= 0) {
            log.warn("Skipping invalid exchange rate message: {}", message);
            return;
        }
        rateProvider.updateRate(message.currency(), message.rateToRub());
        log.debug("Exchange rate updated: 1 {} = {} RUB", message.currency(), message.rateToRub());
    }
}
