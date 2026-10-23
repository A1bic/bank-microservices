package ru.kovalenko.bank.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kovalenko.bank.presentation.models.CurrentUserResponse;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Information about the authenticated user")
public class AuthController {
    private static final String ROLE_PREFIX = "ROLE_";

    @GetMapping("/me")
    @Operation(summary = "Get the current user")
    public CurrentUserResponse me(Authentication authentication) {
        var roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.startsWith(ROLE_PREFIX) ? authority.substring(ROLE_PREFIX.length()) : authority)
                .sorted()
                .toList();
        return new CurrentUserResponse(authentication.getName(), roles);
    }
}
