package org.example.playus.domain.signup.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.playus.domain.employee.PersonalInfo;

@Getter
public class SignupRequestDto {

    private String username;
    private PersonalInfo personalInfo;

    @Builder
    public SignupRequestDto(int conum, String username, PersonalInfo personalInfo) {
        this.username = username;
        this.personalInfo = personalInfo;
    }
}
