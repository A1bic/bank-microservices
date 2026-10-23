package ru.kovalenko.bank.domain.account;

import org.junit.jupiter.api.Test;
import ru.kovalenko.bank.domain.common.Money;
import ru.kovalenko.bank.domain.user.UserId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {
    private static final UserId OWNER = new UserId(1L);

    @Test
    void depositIncreasesBalance() {
        var account = new Account(OWNER, Money.of("100"));

        account.deposit(Money.of("50.25"));

        assertThat(account.getBalance()).isEqualTo(Money.of("150.25"));
    }

    @Test
    void withdrawDecreasesBalance() {
        var account = new Account(OWNER, Money.of("100"));

        account.withdraw(Money.of("100"));

        assertThat(account.getBalance()).isEqualTo(Money.zero());
    }

    @Test
    void withdrawMoreThanBalanceIsRejected() {
        var account = new Account(OWNER, Money.of("100"));

        assertThatThrownBy(() -> account.withdraw(Money.of("100.01")))
                .isInstanceOf(InsufficientFundsException.class);
        assertThat(account.getBalance()).isEqualTo(Money.of("100"));
    }

    @Test
    void nonPositiveAmountsAreRejected() {
        var account = new Account(OWNER, Money.of("100"));

        assertThatThrownBy(() -> account.deposit(Money.zero())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> account.withdraw(Money.of("-1"))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void negativeInitialBalanceIsRejected() {
        assertThatThrownBy(() -> new Account(OWNER, Money.of("-0.01")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void knowsItsOwner() {
        var account = new Account(OWNER, Money.zero());

        assertThat(account.belongsTo(new UserId(1L))).isTrue();
        assertThat(account.belongsTo(new UserId(2L))).isFalse();
    }
}
