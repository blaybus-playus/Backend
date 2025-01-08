package org.example.playus.domain.signup;

import lombok.RequiredArgsConstructor;
import org.example.playus.domain.employee.Account;
import org.example.playus.domain.employee.Employee;
import org.example.playus.domain.employee.EmployeeRepositoryMongo;
import org.example.playus.domain.signup.dto.SignupRequestDto;
import org.example.playus.domain.signup.dto.SignupResponseDto;
import org.example.playus.global.exception.CustomException;
import org.example.playus.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final EmployeeRepositoryMongo employeeRepositoryMongo;

    @Transactional
    public SignupResponseDto signup (SignupRequestDto requestDto) {
        if (employeeRepositoryMongo.existsByPersonalInfoConum(requestDto.getConum())) {
            throw new CustomException(ErrorCode.EMPLOYEE_EXIST);
        }

        if (employeeRepositoryMongo.findByAccountUsername(requestDto.getUsername()).isPresent()) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXIST);
        }

        Account account = Account.builder()
                .username(requestDto.getUsername())
                .defaultPassword(requestDto.getPassword())
                .updatedPassword(null)
                .build();

        Employee employee =new Employee();
        employee.setAccount(account);
        employee.setPersonalInfo(requestDto.getPersonalInfo());
        employee.setPoints(new HashMap<>());

        employeeRepositoryMongo.save(employee);

        return SignupResponseDto.builder()
                .success(true)
                .message("회원가입이 완료되었습니다.")
                .build();
    }


}
