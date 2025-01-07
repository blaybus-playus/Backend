package org.example.playus.domain.login;

import lombok.RequiredArgsConstructor;
import org.example.playus.domain.employee.Account;
import org.example.playus.domain.employee.Employee;
import org.example.playus.domain.employee.EmployeeRepositoryMongo;
import org.example.playus.global.exception.CustomException;
import org.example.playus.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final EmployeeRepositoryMongo employeeRepositoryMongo;

    // TODO : JWT 토큰 발급 로직까지 추가해야 함
    public LoginResponseDto login(LoginRequestDto requestDto) {
        String username = requestDto.getUsername();
        String password = requestDto.getPassword();

        Employee loginEmployee = employeeRepositoryMongo.findByPersonalInfoName(username)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));

        // 비밀번호는 보이지 않고 username만 보여줌
        Account account = Account.builder()
                .username(loginEmployee.getAccount().getUsername())
                .defaultPassword(null)
                .updatedPassword(null)
                .build();



        if(!loginEmployee.getAccount().getDefaultPassword().equals(password)) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_CORRECT);
        } else {
            return LoginResponseDto.builder()
                    .employeeId(loginEmployee.getEmployeeId())
                    .personalInfo(loginEmployee.getPersonalInfo())
                    .account(account)
                    .points(loginEmployee.getPoints())
                    .build();
        }
    }
}
