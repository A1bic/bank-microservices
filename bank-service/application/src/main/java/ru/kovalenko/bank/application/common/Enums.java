package ru.kovalenko.bank.application.common;

import java.util.Locale;
import java.util.Optional;

public final class Enums {
    private Enums() {
    }

    /**
     * Case-insensitive lookup of an enum constant by its name.
     */
    public static <E extends Enum<E>> Optional<E> parse(Class<E> type, String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
