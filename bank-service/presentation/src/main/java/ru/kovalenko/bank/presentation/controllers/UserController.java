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
import ru.kovalenko.bank.application.contracts.users.UserService;
import ru.kovalenko.bank.application.contracts.users.models.UserDto;
import ru.kovalenko.bank.application.contracts.users.operations.CreateUser;
import ru.kovalenko.bank.application.contracts.users.operations.GetAllUsers;
import ru.kovalenko.bank.application.contracts.users.operations.GetUserById;
import ru.kovalenko.bank.application.contracts.users.operations.GetUsersByFilter;
import ru.kovalenko.bank.presentation.models.CreateUserRequest;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Users", description = "User management. Requires ADMIN role")
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a user")
    @ApiResponse(responseCode = "201", description = "User created")
    @ApiResponse(responseCode = "400", description = "Invalid request, unknown gender or role")
    @ApiResponse(responseCode = "409", description = "Login is already taken")
    public UserDto createUser(@Valid @RequestBody CreateUserRequest request) {
        var createUser = new CreateUser.Request(
                request.login(),
                request.password(),
                request.name(),
                request.age(),
                request.gender(),
                request.hairColor(),
                request.role()
        );
        return userService.createUser(createUser).user();
    }

    @GetMapping
    @Operation(summary = "List all users")
    @ApiResponse(responseCode = "200", description = "All users")
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers(new GetAllUsers.Request()).users();
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID")
    @ApiResponse(responseCode = "200", description = "User")
    @ApiResponse(responseCode = "404", description = "User not found")
    public UserDto getUser(@PathVariable long userId) {
        return userService.getUserById(new GetUserById.Request(userId)).user();
    }

    @GetMapping("/search")
    @Operation(summary = "Find users by gender and hair color")
    @ApiResponse(responseCode = "200", description = "Matching users")
    @ApiResponse(responseCode = "400", description = "Missing filter or unknown gender")
    public List<UserDto> searchUsers(
            @Parameter(description = "Genders to include", example = "MALE")
            @RequestParam(name = "gender") Set<String> genders,
            @Parameter(description = "Hair colors to include", example = "black")
            @RequestParam(name = "hairColor") Set<String> hairColors
    ) {
        return userService.getUsersByFilter(new GetUsersByFilter.Request(genders, hairColors)).users();
    }
}
