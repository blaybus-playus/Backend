package org.example.playus.domain.admin;

import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class Admin {
    private Role role; // 권한 (ROLE_USER, ROLE_ADMIN 등)

    public Admin(Role role) {
        this.role = role;
    }

    // Setter (필요 시)
    public void setRole(Role role) {
        this.role = role;
    }
}