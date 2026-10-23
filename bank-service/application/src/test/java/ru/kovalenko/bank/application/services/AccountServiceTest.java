package ru.kovalenko.bank.application.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kovalenko.bank.application.abstractions.persistence.AccountRepository;
import ru.kovalenko.bank.application.abstractions.persistence.TransactionRepository;
import ru.kovalenko.bank.application.abstractions.persistence.UserRepository;
import ru.kovalenko.bank.application.abstractions.rates.ExchangeRateProvider;
import ru.kovalenko.bank.application.contracts.accounts.exceptions.AccountNotFoundException;
import ru.kovalenko.bank.application.contracts.accounts.exceptions.CurrencyNotFoundException;
import ru.kovalenko.bank.application.contracts.accounts.operations.CreateAccount;
import ru.kovalenko.bank.application.contracts.accounts.operations.Deposit;
import ru.kovalenko.bank.application.contracts.accounts.operations.GetBalance;
import ru.kovalenko.bank.application.contracts.accounts.operations.GetTransactions;
import ru.kovalenko.bank.application.contracts.accounts.operations.Transfer;
import ru.kovalenko.bank.application.contracts.accounts.operations.Withdraw;
import ru.kovalenko.bank.application.contracts.users.exceptions.UserNotFoundException;
import ru.kovalenko.bank.domain.account.Account;
import ru.kovalenko.bank.domain.account.AccountId;
import ru.kovalenko.bank.domain.account.InsufficientFundsException;
import ru.kovalenko.bank.domain.common.Money;
import ru.kovalenko.bank.domain.role.RoleId;
import ru.kovalenko.bank.domain.transaction.Transaction;
import ru.kovalenko.bank.domain.transaction.TransactionId;
import ru.kovalenko.bank.domain.transaction.TransactionType;
import ru.kovalenko.bank.domain.user.Gender;
import ru.kovalenko.bank.domain.user.User;
import ru.kovalenko.bank.domain.user.UserId;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private ExchangeRateProvider exchangeRateProvider;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void createAccount_unknownUser_throwsUserNotFound() {
        when(userRepository.existsById(new UserId(1L))).thenReturn(false);

        var request = new CreateAccount.Request(1L, new BigDecimal("100.00"));

        assertThatThrownBy(() -> accountService.createAccount(request)).isInstanceOf(UserNotFoundException.class);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void deposit_validAmount_updatesBalanceAndHistory() {
        Account account = account(10L, user(1L), "700.00");
        givenAccounts(account);
        givenTransactionIdsStartFrom(100L);

        var response = accountService.deposit(new Deposit.Request(10L, new BigDecimal("200.00")));

        assertThat(response.transaction().type()).isEqualTo("DEPOSIT");
        assertThat(response.transaction().amount()).isEqualByComparingTo("200.00");
        assertThat(account.getBalance()).isEqualTo(Money.of("900.00"));
        assertThat(account.getTransactionHistory()).containsExactly(new TransactionId(100L));
        verify(accountRepository).save(account);
    }

    @Test
    void deposit_nonPositiveAmount_isRejected() {
        givenAccounts(account(10L, user(1L), "700.00"));

        var request = new Deposit.Request(10L, BigDecimal.ZERO);

        assertThatThrownBy(() -> accountService.deposit(request)).isInstanceOf(IllegalArgumentException.class);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void withdraw_sufficientBalance_updatesBalanceAndHistory() {
        Account account = account(10L, user(1L), "700.00");
        givenAccounts(account);
        givenTransactionIdsStartFrom(100L);

        var response = accountService.withdraw(new Withdraw.Request(10L, new BigDecimal("300.00")));

        assertThat(response.transaction().type()).isEqualTo("WITHDRAW");
        assertThat(response.transaction().amount()).isEqualByComparingTo("300.00");
        assertThat(account.getBalance()).isEqualTo(Money.of("400.00"));
        assertThat(account.getTransactionHistory()).containsExactly(new TransactionId(100L));
        verify(accountRepository).save(account);
    }

    @Test
    void withdraw_insufficientBalance_throwsAndSavesNothing() {
        givenAccounts(account(10L, user(1L), "700.00"));

        var request = new Withdraw.Request(10L, new BigDecimal("1000.00"));

        assertThatThrownBy(() -> accountService.withdraw(request)).isInstanceOf(InsufficientFundsException.class);
        verify(transactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void withdraw_unknownAccount_throwsAccountNotFound() {
        when(accountRepository.findById(new AccountId(10L))).thenReturn(Optional.empty());

        var request = new Withdraw.Request(10L, new BigDecimal("1.00"));

        assertThatThrownBy(() -> accountService.withdraw(request)).isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void transfer_betweenOwnAccounts_noCommission() {
        User owner = user(1L);
        Account from = account(10L, owner, "700.00");
        Account to = account(20L, owner, "500.00");
        givenAccounts(from, to);
        givenTransactionIdsStartFrom(100L);

        var response = accountService.transfer(new Transfer.Request(10L, 20L, new BigDecimal("300.00")));

        assertThat(response.transaction().type()).isEqualTo("TRANSFER_OUT");
        assertThat(response.transaction().amount()).isEqualByComparingTo("300.00");
        assertThat(response.transaction().commission()).isEqualByComparingTo("0.00");
        assertThat(from.getBalance()).isEqualTo(Money.of("400.00"));
        assertThat(to.getBalance()).isEqualTo(Money.of("800.00"));
    }

    @Test
    void transfer_toFriend_3percentCommission() {
        User sender = user(1L);
        User receiver = user(2L);
        sender.addFriend(receiver.getId());
        Account from = account(10L, sender, "1000.00");
        Account to = account(20L, receiver, "0.00");
        givenAccounts(from, to);
        when(userRepository.findWithFriendsById(sender.getId())).thenReturn(Optional.of(sender));
        givenTransactionIdsStartFrom(100L);

        var response = accountService.transfer(new Transfer.Request(10L, 20L, new BigDecimal("100.00")));

        assertThat(response.transaction().amount()).isEqualByComparingTo("103.00");
        assertThat(response.transaction().commission()).isEqualByComparingTo("3.00");
        assertThat(from.getBalance()).isEqualTo(Money.of("897.00"));
        assertThat(to.getBalance()).isEqualTo(Money.of("100.00"));
    }

    @Test
    void transfer_toStranger_10percentCommission() {
        User sender = user(1L);
        Account from = account(10L, sender, "1000.00");
        Account to = account(20L, user(2L), "0.00");
        givenAccounts(from, to);
        when(userRepository.findWithFriendsById(sender.getId())).thenReturn(Optional.of(sender));
        givenTransactionIdsStartFrom(100L);

        var response = accountService.transfer(new Transfer.Request(10L, 20L, new BigDecimal("100.00")));

        assertThat(response.transaction().amount()).isEqualByComparingTo("110.00");
        assertThat(from.getBalance()).isEqualTo(Money.of("890.00"));
        assertThat(to.getBalance()).isEqualTo(Money.of("100.00"));
    }

    @Test
    void transfer_insufficientFundsIncludingCommission_changesNothing() {
        User sender = user(1L);
        Account from = account(10L, sender, "1000.00");
        Account to = account(20L, user(2L), "0.00");
        givenAccounts(from, to);
        when(userRepository.findWithFriendsById(sender.getId())).thenReturn(Optional.of(sender));

        // 1000 + 10% commission does not fit into the balance of 1000
        var request = new Transfer.Request(10L, 20L, new BigDecimal("1000.00"));

        assertThatThrownBy(() -> accountService.transfer(request)).isInstanceOf(InsufficientFundsException.class);
        assertThat(from.getBalance()).isEqualTo(Money.of("1000.00"));
        assertThat(to.getBalance()).isEqualTo(Money.of("0.00"));
        verify(transactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void transfer_recordsTransactionInHistoryOfBothAccounts() {
        User sender = user(1L);
        Account from = account(10L, sender, "1000.00");
        Account to = account(20L, user(2L), "0.00");
        givenAccounts(from, to);
        when(userRepository.findWithFriendsById(sender.getId())).thenReturn(Optional.of(sender));
        givenTransactionIdsStartFrom(100L);

        accountService.transfer(new Transfer.Request(10L, 20L, new BigDecimal("100.00")));

        var saved = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(saved.capture());
        Transaction outgoing = saved.getAllValues().get(0);
        Transaction incoming = saved.getAllValues().get(1);

        assertThat(outgoing.getTransactionType()).isEqualTo(TransactionType.TRANSFER_OUT);
        assertThat(outgoing.getAccountId()).isEqualTo(from.getId());
        assertThat(outgoing.getCounterpartyAccountId()).isEqualTo(to.getId());

        assertThat(incoming.getTransactionType()).isEqualTo(TransactionType.TRANSFER_IN);
        assertThat(incoming.getAccountId()).isEqualTo(to.getId());
        assertThat(incoming.getCounterpartyAccountId()).isEqualTo(from.getId());

        assertThat(from.getTransactionHistory()).containsExactly(new TransactionId(100L));
        assertThat(to.getTransactionHistory()).containsExactly(new TransactionId(101L));
        verify(accountRepository).save(from);
        verify(accountRepository).save(to);
    }

    @Test
    void transfer_toTheSameAccount_isRejected() {
        var request = new Transfer.Request(10L, 10L, new BigDecimal("100.00"));

        assertThatThrownBy(() -> accountService.transfer(request)).isInstanceOf(IllegalArgumentException.class);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void getBalance_inForeignCurrency_isConvertedWithCurrentRate() {
        givenAccounts(account(10L, user(1L), "1000.00"));
        when(exchangeRateProvider.findRateToRub("USD")).thenReturn(Optional.of(new BigDecimal("80")));

        var response = accountService.getBalance(new GetBalance.Request(10L, "usd"));

        assertThat(response.balance().amount()).isEqualByComparingTo("12.50");
        assertThat(response.balance().currency()).isEqualTo("USD");
    }

    @Test
    void getBalance_unknownCurrency_throwsCurrencyNotFound() {
        givenAccounts(account(10L, user(1L), "1000.00"));
        when(exchangeRateProvider.findRateToRub("EUR")).thenReturn(Optional.empty());

        var request = new GetBalance.Request(10L, "EUR");

        assertThatThrownBy(() -> accountService.getBalance(request)).isInstanceOf(CurrencyNotFoundException.class);
    }

    @Test
    void getTransactions_unknownType_isRejected() {
        when(accountRepository.existsById(new AccountId(10L))).thenReturn(true);

        var request = new GetTransactions.Request(10L, Set.of("REFUND"), "RUB");

        assertThatThrownBy(() -> accountService.getTransactions(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("REFUND");
    }

    private static User user(long id) {
        var user = new User("user" + id, "name", 18, Gender.MALE, "black", new RoleId(2L), "hash");
        user.setId(new UserId(id));
        return user;
    }

    private static Account account(long id, User owner, String balance) {
        var account = new Account(owner.getId(), Money.of(balance));
        account.setId(new AccountId(id));
        return account;
    }

    private void givenAccounts(Account... accounts) {
        for (Account account : accounts) {
            when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        }
    }

    /**
     * Imitates the database assigning ids to saved transactions.
     */
    private void givenTransactionIdsStartFrom(long firstId) {
        var nextId = new AtomicLong(firstId);
        when(transactionRepository.save(any())).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(new TransactionId(nextId.getAndIncrement()));
            return transaction;
        });
    }
}
