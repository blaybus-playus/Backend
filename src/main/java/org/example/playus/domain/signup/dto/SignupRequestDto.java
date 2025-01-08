package org.example.playus.domain.signup.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.playus.domain.employee.PersonalInfo;

@Getter
public class SignupRequestDto {

    private int conum;
    private String username;
    private PersonalInfo personalInfo;
    private String password;

    @Builder
    public SignupRequestDto(int conum, String username, String password, PersonalInfo personalInfo) {
        this.conum = conum;
        this.username = username;
        this.personalInfo = personalInfo;
        this.password = password;
    }
}
