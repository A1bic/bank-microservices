package ru.kovalenko.bank.infrastructure.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.kovalenko.bank.application.abstractions.persistence.RoleRepository;
import ru.kovalenko.bank.application.abstractions.persistence.UserRepository;
import ru.kovalenko.bank.domain.role.Role;
import ru.kovalenko.bank.domain.role.RoleName;
import ru.kovalenko.bank.domain.user.Gender;
import ru.kovalenko.bank.domain.user.User;

/**
 * Creates the administrator account on the first start, so there is someone who can create other users.
 * Roles themselves are inserted by the Flyway migration.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(RoleName.ADMIN)) {
            return;
        }
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException("Role ADMIN is missing, check database migrations"));

        User admin = new User(
                adminProperties.login(),
                "Administrator",
                30,
                Gender.MALE,
                "none",
                adminRole.getId(),
                passwordEncoder.encode(adminProperties.password())
        );
        userRepository.save(admin);
        log.info("Created administrator '{}'", adminProperties.login());
    }
}
