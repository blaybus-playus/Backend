package org.example.playus.domain.employee.model;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Account {
    private String username;         // 아이디
    private String defaultPassword;  // 기본 패스워드
    private String updatedPassword;  // 변경된 패스워드

    // Getters and Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    public void setUpdatedPassword(String updatedPassword) {
        this.updatedPassword = updatedPassword;
    }

    @Builder
    public Account(String username, String defaultPassword, String updatedPassword) {
        this.username = username;
        this.defaultPassword = defaultPassword;
        this.updatedPassword = updatedPassword;
    }
}

