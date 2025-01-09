package org.example.playus.domain.signup.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponseDto {
    private boolean success;
    private String message;
}
