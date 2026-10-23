package ru.kovalenko.bank.application.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kovalenko.bank.application.abstractions.persistence.AccountRepository;
import ru.kovalenko.bank.application.abstractions.persistence.TransactionRepository;
import ru.kovalenko.bank.application.abstractions.persistence.UserRepository;
import ru.kovalenko.bank.application.abstractions.rates.ExchangeRateProvider;
import ru.kovalenko.bank.application.common.Enums;
import ru.kovalenko.bank.application.contracts.accounts.AccountService;
import ru.kovalenko.bank.application.contracts.accounts.exceptions.AccountNotFoundException;
import ru.kovalenko.bank.application.contracts.accounts.exceptions.CurrencyNotFoundException;
import ru.kovalenko.bank.application.contracts.accounts.models.MoneyDto;
import ru.kovalenko.bank.application.contracts.accounts.operations.CreateAccount;
import ru.kovalenko.bank.application.contracts.accounts.operations.Deposit;
import ru.kovalenko.bank.application.contracts.accounts.operations.GetAccountById;
import ru.kovalenko.bank.application.contracts.accounts.operations.GetAccountsByUserId;
import ru.kovalenko.bank.application.contracts.accounts.operations.GetBalance;
import ru.kovalenko.bank.application.contracts.accounts.operations.GetTransactions;
import ru.kovalenko.bank.application.contracts.accounts.operations.Transfer;
import ru.kovalenko.bank.application.contracts.accounts.operations.Withdraw;
import ru.kovalenko.bank.application.contracts.users.exceptions.UserNotFoundException;
import ru.kovalenko.bank.application.mapping.AccountDtoMapper;
import ru.kovalenko.bank.application.mapping.CurrencyConversion;
import ru.kovalenko.bank.application.mapping.TransactionDtoMapper;
import ru.kovalenko.bank.domain.account.Account;
import ru.kovalenko.bank.domain.account.AccountId;
import ru.kovalenko.bank.domain.common.Money;
import ru.kovalenko.bank.domain.transaction.Transaction;
import ru.kovalenko.bank.domain.transaction.TransactionType;
import ru.kovalenko.bank.domain.user.User;
import ru.kovalenko.bank.domain.user.UserId;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    /** Commission for transfers to a friend's account. */
    static final BigDecimal FRIEND_COMMISSION_RATE = new BigDecimal("0.03");

    /** Commission for transfers to any other user's account. */
    static final BigDecimal STRANGER_COMMISSION_RATE = new BigDecimal("0.10");

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final ExchangeRateProvider exchangeRateProvider;

    @Override
    @Transactional
    public CreateAccount.Response createAccount(CreateAccount.Request request) {
        UserId ownerId = requireUserExists(request.userId());
        Account account = accountRepository.save(new Account(ownerId, Money.of(request.initialBalance())));
        return new CreateAccount.Response(AccountDtoMapper.toDto(account));
    }

    @Override
    @Transactional(readOnly = true)
    public GetAccountById.Response getAccountById(GetAccountById.Request request) {
        Account account = requireAccount(request.accountId());
        return new GetAccountById.Response(AccountDtoMapper.toDto(account));
    }

    @Override
    @Transactional(readOnly = true)
    public GetAccountsByUserId.Response getAccountsByUserId(GetAccountsByUserId.Request request) {
        UserId ownerId = requireUserExists(request.userId());
        var accounts = accountRepository.findByOwnerId(ownerId).stream()
                .map(AccountDtoMapper::toDto)
                .toList();
        return new GetAccountsByUserId.Response(accounts);
    }

    @Override
    @Transactional(readOnly = true)
    public GetBalance.Response getBalance(GetBalance.Request request) {
        Account account = requireAccount(request.accountId());
        CurrencyConversion conversion = conversionTo(request.currency());
        var balance = new MoneyDto(conversion.convert(account.getBalance()), conversion.currency());
        return new GetBalance.Response(balance);
    }

    @Override
    @Transactional(readOnly = true)
    public GetTransactions.Response getTransactions(GetTransactions.Request request) {
        AccountId accountId = new AccountId(request.accountId());
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(request.accountId());
        }
        Set<TransactionType> types = parseTransactionTypes(request.types());
        CurrencyConversion conversion = conversionTo(request.currency());

        var transactions = transactionRepository.findByAccountIdAndTypes(accountId, types).stream()
                .map(transaction -> TransactionDtoMapper.toDto(transaction, conversion))
                .toList();
        return new GetTransactions.Response(transactions);
    }

    @Override
    @Transactional
    public Deposit.Response deposit(Deposit.Request request) {
        Money amount = Money.of(request.amount());
        Account account = requireAccount(request.accountId());

        account.deposit(amount);
        Transaction transaction = transactionRepository.save(Transaction.deposit(account.getId(), amount));
        account.recordTransaction(transaction.getId());
        accountRepository.save(account);

        return new Deposit.Response(TransactionDtoMapper.toDto(transaction));
    }

    @Override
    @Transactional
    public Withdraw.Response withdraw(Withdraw.Request request) {
        Money amount = Money.of(request.amount());
        Account account = requireAccount(request.accountId());

        account.withdraw(amount);
        Transaction transaction = transactionRepository.save(Transaction.withdrawal(account.getId(), amount));
        account.recordTransaction(transaction.getId());
        accountRepository.save(account);

        return new Withdraw.Response(TransactionDtoMapper.toDto(transaction));
    }

    @Override
    @Transactional
    public Transfer.Response transfer(Transfer.Request request) {
        if (request.fromAccountId() == request.toAccountId()) {
            throw new IllegalArgumentException("Cannot transfer money to the same account");
        }
        Money amount = Money.of(request.amount());
        Account from = requireAccount(request.fromAccountId());
        Account to = requireAccount(request.toAccountId());

        Money commission = calculateCommission(from, to, amount);
        Money totalDebit = amount.add(commission);

        from.withdraw(totalDebit);
        to.deposit(amount);

        Transaction outgoing = transactionRepository.save(
                Transaction.transferOut(from.getId(), to.getId(), totalDebit, commission));
        Transaction incoming = transactionRepository.save(
                Transaction.transferIn(from.getId(), to.getId(), amount));
        from.recordTransaction(outgoing.getId());
        to.recordTransaction(incoming.getId());

        accountRepository.save(from);
        accountRepository.save(to);

        return new Transfer.Response(TransactionDtoMapper.toDto(outgoing));
    }

    /**
     * Transfers between own accounts are free, to a friend cost 3%, to anyone else 10%.
     */
    private Money calculateCommission(Account from, Account to, Money amount) {
        if (to.belongsTo(from.getOwnerId())) {
            return Money.zero();
        }
        User sender = userRepository.findWithFriendsById(from.getOwnerId())
                .orElseThrow(() -> new UserNotFoundException(from.getOwnerId().value()));
        BigDecimal rate = sender.isFriendWith(to.getOwnerId()) ? FRIEND_COMMISSION_RATE : STRANGER_COMMISSION_RATE;
        return amount.multiply(rate);
    }

    private Account requireAccount(long accountId) {
        return accountRepository.findById(new AccountId(accountId))
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private UserId requireUserExists(long userId) {
        UserId id = new UserId(userId);
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(userId);
        }
        return id;
    }

    private CurrencyConversion conversionTo(String currency) {
        if (currency == null || currency.isBlank()) {
            return CurrencyConversion.none();
        }
        String code = currency.trim().toUpperCase(Locale.ROOT);
        if (CurrencyConversion.BASE_CURRENCY.equals(code)) {
            return CurrencyConversion.none();
        }
        BigDecimal rate = exchangeRateProvider.findRateToRub(code)
                .orElseThrow(() -> new CurrencyNotFoundException(code));
        return new CurrencyConversion(code, rate);
    }

    private static Set<TransactionType> parseTransactionTypes(Set<String> types) {
        if (types == null || types.isEmpty()) {
            return EnumSet.allOf(TransactionType.class);
        }
        return types.stream()
                .map(type -> Enums.parse(TransactionType.class, type).orElseThrow(() ->
                        new IllegalArgumentException("Unknown transaction type: " + type
                                + ". Allowed values: " + Arrays.toString(TransactionType.values()))))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(TransactionType.class)));
    }
}
