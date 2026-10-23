package ru.kovalenko.bank.infrastructure.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Credentials of the administrator created on the first start.
 */
@ConfigurationProperties(prefix = "bank.admin")
public record AdminProperties(String login, String password) {
}
