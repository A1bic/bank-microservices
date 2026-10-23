package ru.kovalenko.bank.application.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kovalenko.bank.application.abstractions.persistence.RoleRepository;
import ru.kovalenko.bank.application.abstractions.persistence.UserRepository;
import ru.kovalenko.bank.domain.role.Role;
import ru.kovalenko.bank.domain.user.User;

/**
 * Loads users for Spring Security authentication (form login and HTTP Basic).
 */
@Service
@RequiredArgsConstructor
public class BankUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + login));
        Role role = roleRepository.findById(user.getRole())
                .orElseThrow(() -> new IllegalStateException("User " + login + " has no role"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getLogin())
                .password(user.getPasswordHash())
                .roles(role.getName().name())
                .build();
    }
}
