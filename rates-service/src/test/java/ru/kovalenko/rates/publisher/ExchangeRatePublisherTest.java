package ru.kovalenko.rates.publisher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import ru.kovalenko.rates.config.RatesProperties;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExchangeRatePublisherTest {
    @Mock
    private KafkaTemplate<String, ExchangeRateMessage> kafkaTemplate;

    @Test
    void publishesRateWithinConfiguredRange() {
        var properties = new RatesProperties("exchange-rates", "USD", new BigDecimal("80"), new BigDecimal("100"));
        var publisher = new ExchangeRatePublisher(kafkaTemplate, properties);

        publisher.publishRate();

        var message = ArgumentCaptor.forClass(ExchangeRateMessage.class);
        verify(kafkaTemplate).send(eq("exchange-rates"), eq("USD"), message.capture());
        assertThat(message.getValue().currency()).isEqualTo("USD");
        assertThat(message.getValue().rateToRub()).isBetween(new BigDecimal("80"), new BigDecimal("100"));
        assertThat(message.getValue().timestamp()).isNotNull();
    }

    @Test
    void rejectsInvalidRange() {
        assertThatThrownBy(() -> new RatesProperties("topic", "USD", new BigDecimal("100"), new BigDecimal("80")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
