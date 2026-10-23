package ru.kovalenko.bank.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.kovalenko.bank.application.contracts.users.UserService;
import ru.kovalenko.bank.application.contracts.users.models.UserDto;
import ru.kovalenko.bank.application.contracts.users.operations.AddFriend;
import ru.kovalenko.bank.application.contracts.users.operations.GetFriends;
import ru.kovalenko.bank.application.contracts.users.operations.RemoveFriend;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/friends")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
@Tag(name = "Friends", description = "Friendships between users. Transfers to friends have a lower commission. "
        + "Requires CLIENT role")
public class FriendController {
    private final UserService userService;

    @GetMapping
    @Operation(summary = "List friends of the user")
    @ApiResponse(responseCode = "200", description = "Friends")
    @ApiResponse(responseCode = "404", description = "User not found")
    public List<UserDto> getFriends(@PathVariable long userId) {
        return userService.getFriends(new GetFriends.Request(userId)).friends();
    }

    @PostMapping("/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Add a friend", description = "Friendship is mutual")
    @ApiResponse(responseCode = "204", description = "Friend added")
    @ApiResponse(responseCode = "400", description = "User cannot befriend themselves")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "409", description = "Users are already friends")
    public void addFriend(@PathVariable long userId, @PathVariable long friendId) {
        userService.addFriend(new AddFriend.Request(userId, friendId));
    }

    @DeleteMapping("/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a friend")
    @ApiResponse(responseCode = "204", description = "Friend removed")
    @ApiResponse(responseCode = "400", description = "Users are not friends")
    @ApiResponse(responseCode = "404", description = "User not found")
    public void removeFriend(@PathVariable long userId, @PathVariable long friendId) {
        userService.removeFriend(new RemoveFriend.Request(userId, friendId));
    }
}
