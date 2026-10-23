package ru.kovalenko.bank.domain.common;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyTest {

    @Test
    void amountIsRoundedToTwoDecimalPlaces() {
        assertThat(Money.of("10.005").getAmount()).isEqualByComparingTo("10.01");
        assertThat(Money.of("10").getAmount()).hasToString("10.00");
    }

    @Test
    void equalityDoesNotDependOnScale() {
        assertThat(Money.of("10")).isEqualTo(Money.of("10.00"));
        assertThat(Money.of("10")).hasSameHashCodeAs(Money.of("10.00"));
    }

    @Test
    void arithmetic() {
        assertThat(Money.of("100.50").add(Money.of("0.50"))).isEqualTo(Money.of("101"));
        assertThat(Money.of("100").subtract(Money.of("0.01"))).isEqualTo(Money.of("99.99"));
        assertThat(Money.of("33.33").multiply(new BigDecimal("0.10"))).isEqualTo(Money.of("3.33"));
    }

    @Test
    void comparisons() {
        assertThat(Money.of("1").isLessThan(Money.of("2"))).isTrue();
        assertThat(Money.of("2").isLessThan(Money.of("2"))).isFalse();
        assertThat(Money.of("0.01").isPositive()).isTrue();
        assertThat(Money.zero().isPositive()).isFalse();
        assertThat(Money.of("-1").isNegative()).isTrue();
    }
}
