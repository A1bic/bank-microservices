package ru.kovalenko.bank.presentation.errors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.kovalenko.bank.application.contracts.accounts.exceptions.AccountNotFoundException;
import ru.kovalenko.bank.application.contracts.accounts.exceptions.CurrencyNotFoundException;
import ru.kovalenko.bank.application.contracts.users.exceptions.GenderNotFoundException;
import ru.kovalenko.bank.application.contracts.users.exceptions.RoleNotFoundException;
import ru.kovalenko.bank.application.contracts.users.exceptions.UserAlreadyExistsException;
import ru.kovalenko.bank.application.contracts.users.exceptions.UserNotFoundException;
import ru.kovalenko.bank.application.contracts.users.exceptions.UsersAlreadyFriendsException;
import ru.kovalenko.bank.application.contracts.users.exceptions.UsersNotFriendsException;
import ru.kovalenko.bank.domain.account.InsufficientFundsException;

/**
 * Turns exceptions into RFC 9457 problem details: {@code {"title", "status", "detail", "errorCode", ...}}.
 * Validation and other Spring MVC errors are handled by {@link ResponseEntityExceptionHandler}.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleAccountNotFound(AccountNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Account not found", "ACCOUNT_NOT_FOUND", ex);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "User not found", "USER_NOT_FOUND", ex);
    }

    @ExceptionHandler(CurrencyNotFoundException.class)
    public ProblemDetail handleCurrencyNotFound(CurrencyNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Currency not found", "CURRENCY_NOT_FOUND", ex);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, "User already exists", "USER_ALREADY_EXISTS", ex);
    }

    @ExceptionHandler(UsersAlreadyFriendsException.class)
    public ProblemDetail handleUsersAlreadyFriends(UsersAlreadyFriendsException ex) {
        return problem(HttpStatus.CONFLICT, "Users are already friends", "USERS_ALREADY_FRIENDS", ex);
    }

    @ExceptionHandler(UsersNotFriendsException.class)
    public ProblemDetail handleUsersNotFriends(UsersNotFriendsException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Users are not friends", "USERS_NOT_FRIENDS", ex);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ProblemDetail handleInsufficientFunds(InsufficientFundsException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Insufficient funds", "INSUFFICIENT_FUNDS", ex);
    }

    @ExceptionHandler(GenderNotFoundException.class)
    public ProblemDetail handleGenderNotFound(GenderNotFoundException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Unknown gender", "UNKNOWN_GENDER", ex);
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ProblemDetail handleRoleNotFound(RoleNotFoundException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Unknown role", "UNKNOWN_ROLE", ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", "BAD_REQUEST", ex);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, "Access denied", "FORBIDDEN", ex);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
        problem.setTitle("Internal server error");
        problem.setProperty("errorCode", "INTERNAL_ERROR");
        return problem;
    }

    private static ProblemDetail problem(HttpStatus status, String title, String errorCode, Exception ex) {
        var problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problem.setTitle(title);
        problem.setProperty("errorCode", errorCode);
        return problem;
    }
}
