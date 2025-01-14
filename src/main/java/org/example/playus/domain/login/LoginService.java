package org.example.playus.domain.login;

import lombok.RequiredArgsConstructor;
import org.example.playus.domain.admin.Role;
import org.example.playus.domain.employee.model.Account;
import org.example.playus.domain.employee.model.Employee;
import org.example.playus.domain.employee.EmployeeRepositoryMongo;
import org.example.playus.domain.employee.model.TokenStore;
import org.example.playus.domain.login.dto.LoginRequestDto;
import org.example.playus.domain.login.dto.LoginResponseDto;
import org.example.playus.domain.security.jwt.JwtUtil;
import org.example.playus.global.exception.CustomException;
import org.example.playus.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final EmployeeRepositoryMongo employeeRepositoryMongo;
    private final JwtUtil jwtUtil;

    // TODO : JWT 토큰 발급 로직까지 추가해야 함
    public LoginResponseDto login(LoginRequestDto requestDto) {
        String username = requestDto.getUsername();
        String password = requestDto.getPassword();

        Employee loginEmployee = employeeRepositoryMongo.findByAccountUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));

        if(!loginEmployee.getAccount().getDefaultPassword().equals(password)) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_CORRECT);
        }

        Account account = Account.builder()
                .username(loginEmployee.getAccount().getUsername())
                .defaultPassword(null)
                .updatedPassword(null)
                .build();

        Role role = loginEmployee.getAdmin() != null ? loginEmployee.getAdmin().getRole() : Role.ROLE_USER;  // 기본 ROLE_USER로 설정

        String accessToken = jwtUtil.createToken(username, role, JwtUtil.ACCESS_TOKEN_EXPIRATION);
        String refreshToken = jwtUtil.createToken(username, role, JwtUtil.REFRESH_TOKEN_EXPIRATION);

        TokenStore tokenStore = TokenStore.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        loginEmployee.setTokenStore(tokenStore);
        employeeRepositoryMongo.save(loginEmployee);

        return LoginResponseDto.builder()
                .employeeId(loginEmployee.getEmployeeId())
                .personalInfo(loginEmployee.getPersonalInfo())
                .account(account)
                .points(loginEmployee.getPoints())
                .tokenStore(tokenStore)
                .build();

    }
}
