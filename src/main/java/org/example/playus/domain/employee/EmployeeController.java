package org.example.playus.domain.employee;

import lombok.RequiredArgsConstructor;
import org.example.playus.domain.employee.dto.EmployeeExpDetailResponseDto;
import org.example.playus.domain.employee.dto.EmployeeExpReponseDto;
import org.example.playus.domain.employee.model.Employee;
import org.example.playus.domain.security.service.UserDetailsImpl;
import org.example.playus.global.common.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/info/exp")
    public ResponseEntity<CommonResponse> getEmployeeExp(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Employee employee = userDetails.getEmployee();
            EmployeeExpReponseDto responseDto = employeeService.getEmployeeExp(Integer.parseInt(employee.getEmployeeId()));
            CommonResponse response = new CommonResponse<>("회원 경험치 조회", 200, responseDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            CommonResponse response = new CommonResponse<>("회원 경험치 조회 실패", 500, e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/exp")
    public ResponseEntity<CommonResponse> getEmployeeExpByYear(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam int year) {
        try {
            Employee employee = userDetails.getEmployee();
            EmployeeExpDetailResponseDto responseDto = employeeService.getEmployeeExpByYear(Integer.parseInt(employee.getEmployeeId()), year);
            CommonResponse response = new CommonResponse<>("회원 연도별 경험치 조회", 200, responseDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            CommonResponse response = new CommonResponse<>("회원 연도별 경험치 조회 실패", 500, e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
