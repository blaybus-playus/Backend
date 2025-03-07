package org.example.playus.domain.login.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class LoginRequestDto {
    private final String username;
    private final String password;

    @JsonCreator
    public LoginRequestDto(
            @JsonProperty("username") String username,
            @JsonProperty("password") String password
    ) {
        this.username = username;
        this.password = password;
    }
}