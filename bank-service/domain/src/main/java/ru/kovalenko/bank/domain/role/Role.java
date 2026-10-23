package ru.kovalenko.bank.domain.role;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Role {
    @Setter
    private RoleId id;

    private final RoleName name;

    public Role(RoleName name) {
        this.name = name;
    }
}
