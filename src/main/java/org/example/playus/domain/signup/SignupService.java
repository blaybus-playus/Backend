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
import java.util.List;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final EmployeeRepositoryMongo employeeRepositoryMongo;

    @Transactional
    public SignupResponseDto signup (SignupRequestDto requestDto) {
        if (employeeRepositoryMongo.findByAccountUsername(requestDto.getUsername()).isPresent()) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXIST);
        }

        Account account = Account.builder()
                .username(requestDto.getUsername())
                .defaultPassword(requestDto.getPassword())
                .updatedPassword(null)
                .build();

        String joinDate = requestDto.getPersonalInfo().getJoinDate();
        String employeeId = generateEmployeeId(joinDate);

        Employee employee =new Employee();
        employee.setEmployeeId(employeeId);
        employee.setAccount(account);
        employee.setPersonalInfo(requestDto.getPersonalInfo());
        employee.setPoints(new HashMap<>());

        employeeRepositoryMongo.save(employee);

        return SignupResponseDto.builder()
                .success(true)
                .message("회원가입이 완료되었습니다.")
                .build();
    }

    private String generateEmployeeId(String joinDate) {
        // `joinDate`를 "yyyyMMdd" 형식으로 변환
        String formattedDate = joinDate.replace("-", "");  // 예: 2019-09-01 → 20190901

        // 해당 입사일의 직원 목록 조회
        List<Employee> employees = employeeRepositoryMongo.findByPersonalInfoJoinDate(joinDate);

        // 사번 중 최대값을 찾음 (없으면 0부터 시작)
        int maxNumber = employees.stream()
                .map(e -> e.getEmployeeId().substring(8))  // yyyyMMdd 이후의 2자리 숫자 추출
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);  // 사번이 없으면 0

        // 새로운 사번 생성: maxNumber + 1
        int newNumber = maxNumber + 1;
        String formattedNumber = String.format("%02d", newNumber);  // 두 자리 숫자로 포맷

        // 최종 사번 반환 (예: 2019090101)
        return formattedDate + formattedNumber;
    }
}
