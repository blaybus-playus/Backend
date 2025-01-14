package org.example.playus.domain.logout;

import org.example.playus.domain.employee.model.Employee;
import org.example.playus.domain.employee.EmployeeRepositoryMongo;
import org.example.playus.global.common.CommonResponse;
import org.example.playus.global.exception.CustomException;
import org.example.playus.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class LogoutService {

    private final EmployeeRepositoryMongo employeeRepositoryMongo;

    public LogoutService(EmployeeRepositoryMongo employeeRepositoryMongo) {
        this.employeeRepositoryMongo = employeeRepositoryMongo;
    }

    public CommonResponse<String> logout(String username) {
        // 사용자 조회
        Employee employee = employeeRepositoryMongo.findByAccountUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));

        // `tokenStore` 컬럼 초기화하여 모든 토큰 제거
        if (employee.getTokenStore() != null && !employee.getTokenStore().isEmpty()) {
            employee.getTokenStore().clearTokens();  // 모든 토큰 제거
        } else {
            throw new CustomException(ErrorCode.FALSE_TOKEN);
        }

        // DB에 변경 사항 저장
        employeeRepositoryMongo.save(employee);

        return new CommonResponse<>("로그아웃 되었습니다.", 200, "SUCCESS");
    }
}
