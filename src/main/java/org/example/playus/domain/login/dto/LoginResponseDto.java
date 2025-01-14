package org.example.playus.domain.login.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.example.playus.domain.employee.model.PersonalInfo;
import org.example.playus.domain.employee.model.Account;
import org.example.playus.domain.employee.model.TokenStore;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class LoginResponseDto {
    private String employeeId;
    private PersonalInfo personalInfo;
    private Account account;
    private TokenStore tokenStore;
    private Map<String, Integer> points;

    @Builder
    public LoginResponseDto(String employeeId, PersonalInfo personalInfo, Account account,TokenStore tokenStore ,Map<String, Integer> points) {
        this.employeeId = employeeId;
        this.personalInfo = personalInfo;
        this.account = account;
        this.tokenStore = tokenStore;
        this.points = points;
    }
}