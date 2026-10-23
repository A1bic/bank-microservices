package ru.kovalenko.bank.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.kovalenko.bank.application.contracts.accounts.AccountService;
import ru.kovalenko.bank.application.contracts.accounts.models.AccountDto;
import ru.kovalenko.bank.application.contracts.accounts.models.MoneyDto;
import ru.kovalenko.bank.application.contracts.accounts.operations.CreateAccount;
import ru.kovalenko.bank.application.contracts.accounts.operations.Deposit;
import ru.kovalenko.bank.application.contracts.accounts.operations.GetAccountById;
import ru.kovalenko.bank.application.contracts.accounts.operations.GetAccountsByUserId;
import ru.kovalenko.bank.application.contracts.accounts.operations.GetBalance;
import ru.kovalenko.bank.application.contracts.accounts.operations.GetTransactions;
import ru.kovalenko.bank.application.contracts.accounts.operations.Transfer;
import ru.kovalenko.bank.application.contracts.accounts.operations.Withdraw;
import ru.kovalenko.bank.application.contracts.transactions.models.TransactionDto;
import ru.kovalenko.bank.presentation.models.AmountRequest;
import ru.kovalenko.bank.presentation.models.CreateAccountRequest;
import ru.kovalenko.bank.presentation.models.TransferRequest;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
@Tag(name = "Accounts", description = "Accounts, balances and money operations. Requires CLIENT role")
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/users/{userId}/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Open an account for the user")
    @ApiResponse(responseCode = "201", description = "Account opened")
    @ApiResponse(responseCode = "400", description = "Invalid initial balance")
    @ApiResponse(responseCode = "404", description = "User not found")
    public AccountDto createAccount(@PathVariable long userId, @Valid @RequestBody CreateAccountRequest request) {
        var response = accountService.createAccount(new CreateAccount.Request(userId, request.initialBalance()));
        return response.account();
    }

    @GetMapping("/users/{userId}/accounts")
    @Operation(summary = "List accounts of the user")
    @ApiResponse(responseCode = "200", description = "Accounts of the user")
    @ApiResponse(responseCode = "404", description = "User not found")
    public List<AccountDto> getUserAccounts(@PathVariable long userId) {
        return accountService.getAccountsByUserId(new GetAccountsByUserId.Request(userId)).accounts();
    }

    @GetMapping("/accounts/{accountId}")
    @Operation(summary = "Get account by ID")
    @ApiResponse(responseCode = "200", description = "Account")
    @ApiResponse(responseCode = "404", description = "Account not found")
    public AccountDto getAccount(@PathVariable long accountId) {
        return accountService.getAccountById(new GetAccountById.Request(accountId)).account();
    }

    @GetMapping("/accounts/{accountId}/balance")
    @Operation(summary = "Get account balance",
            description = "Balance is stored in rubles and can be converted to any currency published by rates-service")
    @ApiResponse(responseCode = "200", description = "Balance in the requested currency")
    @ApiResponse(responseCode = "404", description = "Account not found or exchange rate is not available yet")
    public MoneyDto getBalance(
            @PathVariable long accountId,
            @Parameter(description = "Currency code", example = "USD")
            @RequestParam(defaultValue = "RUB") String currency
    ) {
        return accountService.getBalance(new GetBalance.Request(accountId, currency)).balance();
    }

    @GetMapping("/accounts/{accountId}/transactions")
    @Operation(summary = "Get account history", description = "Newest first, optionally filtered by type")
    @ApiResponse(responseCode = "200", description = "Account history")
    @ApiResponse(responseCode = "400", description = "Unknown transaction type")
    @ApiResponse(responseCode = "404", description = "Account not found or exchange rate is not available yet")
    public List<TransactionDto> getTransactions(
            @PathVariable long accountId,
            @Parameter(description = "Transaction types to include, all types when omitted", example = "DEPOSIT")
            @RequestParam(name = "type", required = false) Set<String> types,
            @Parameter(description = "Currency code", example = "USD")
            @RequestParam(defaultValue = "RUB") String currency
    ) {
        var request = new GetTransactions.Request(accountId, types, currency);
        return accountService.getTransactions(request).transactions();
    }

    @PostMapping("/accounts/{accountId}/deposit")
    @Operation(summary = "Deposit money to the account")
    @ApiResponse(responseCode = "200", description = "Deposit transaction")
    @ApiResponse(responseCode = "400", description = "Invalid amount")
    @ApiResponse(responseCode = "404", description = "Account not found")
    public TransactionDto deposit(@PathVariable long accountId, @Valid @RequestBody AmountRequest request) {
        return accountService.deposit(new Deposit.Request(accountId, request.amount())).transaction();
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    @Operation(summary = "Withdraw money from the account")
    @ApiResponse(responseCode = "200", description = "Withdrawal transaction")
    @ApiResponse(responseCode = "400", description = "Invalid amount or insufficient funds")
    @ApiResponse(responseCode = "404", description = "Account not found")
    public TransactionDto withdraw(@PathVariable long accountId, @Valid @RequestBody AmountRequest request) {
        return accountService.withdraw(new Withdraw.Request(accountId, request.amount())).transaction();
    }

    @PostMapping("/transfers")
    @Operation(summary = "Transfer money between accounts",
            description = "Commission is charged to the sender on top of the amount: "
                    + "0% between own accounts, 3% to a friend, 10% to anyone else")
    @ApiResponse(responseCode = "200", description = "Outgoing transaction of the sender")
    @ApiResponse(responseCode = "400", description = "Invalid amount or insufficient funds")
    @ApiResponse(responseCode = "404", description = "Account not found")
    public TransactionDto transfer(@Valid @RequestBody TransferRequest request) {
        var transfer = new Transfer.Request(request.fromAccountId(), request.toAccountId(), request.amount());
        return accountService.transfer(transfer).transaction();
    }
}
