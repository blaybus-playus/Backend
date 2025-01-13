package org.example.playus.domain.employee;

import lombok.RequiredArgsConstructor;
import org.example.playus.global.common.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/info/exp")
    public ResponseEntity<CommonResponse> getEmployeeExp() {
        try {
            int employeeId = 2018020101;
            EmployeeExpReponseDto responseDto = employeeService.getEmployeeExp(employeeId);
            CommonResponse response = new CommonResponse<>("회원 경험치 조회", 200, responseDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            CommonResponse response = new CommonResponse<>("회원 경험치 조회 실패", 500, null);
            return ResponseEntity.status(500).body(response);
        }
    }
}
