package ru.kovalenko.bank.application.contracts.accounts;

import ru.kovalenko.bank.application.contracts.accounts.operations.CreateAccount;
import ru.kovalenko.bank.application.contracts.accounts.operations.Deposit;
import ru.kovalenko.bank.application.contracts.accounts.operations.GetAccountById;
import ru.kovalenko.bank.application.contracts.accounts.operations.GetAccountsByUserId;
import ru.kovalenko.bank.application.contracts.accounts.operations.GetBalance;
import ru.kovalenko.bank.application.contracts.accounts.operations.GetTransactions;
import ru.kovalenko.bank.application.contracts.accounts.operations.Transfer;
import ru.kovalenko.bank.application.contracts.accounts.operations.Withdraw;

public interface AccountService {
    CreateAccount.Response createAccount(CreateAccount.Request request);

    GetAccountById.Response getAccountById(GetAccountById.Request request);

    GetAccountsByUserId.Response getAccountsByUserId(GetAccountsByUserId.Request request);

    GetBalance.Response getBalance(GetBalance.Request request);

    GetTransactions.Response getTransactions(GetTransactions.Request request);

    Deposit.Response deposit(Deposit.Request request);

    Withdraw.Response withdraw(Withdraw.Request request);

    Transfer.Response transfer(Transfer.Request request);
}
