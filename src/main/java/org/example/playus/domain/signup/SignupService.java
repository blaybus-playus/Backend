package org.example.playus.domain.signup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.playus.domain.employee.Account;
import org.example.playus.domain.employee.Employee;
import org.example.playus.domain.employee.EmployeeRepositoryMongo;
import org.example.playus.domain.sheet.GoogleSheetsHelper;
import org.example.playus.domain.signup.dto.SignupRequestDto;
import org.example.playus.domain.signup.dto.SignupResponseDto;
import org.example.playus.global.exception.CustomException;
import org.example.playus.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SignupService {

    private final EmployeeRepositoryMongo employeeRepositoryMongo;

    @Value("${google.spreadsheet.id}")
    private String spreadsheetId;

    private final String range = "시트10!B3:V";

    private static final GoogleSheetsHelper googleSheetsHelper = new GoogleSheetsHelper();

    @Transactional
    public SignupResponseDto signup(SignupRequestDto requestDto) {
        log.info("Starting signup process for username: {}", requestDto.getUsername());

        if (employeeRepositoryMongo.findByAccountUsername(requestDto.getUsername()).isPresent()) {
            log.error("User already exists with username: {}", requestDto.getUsername());
            throw new CustomException(ErrorCode.USER_ALREADY_EXIST);
        }

        Account account = Account.builder()
                .username(requestDto.getUsername())
                .defaultPassword(requestDto.getPassword())
                .updatedPassword("")
                .build();

        String joinDate = requestDto.getPersonalInfo().getJoinDate();
        String employeeId = generateEmployeeId(joinDate);

        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);
        employee.setAccount(account);
        employee.setPersonalInfo(requestDto.getPersonalInfo());
        employee.setPoints(initializePoints());

        try {
            employeeRepositoryMongo.save(employee);
            log.info("Successfully saved employee to MongoDB with ID: {}", employeeId);
        } catch (Exception e) {
            log.error("Failed to save employee to MongoDB: ", e);
            throw new RuntimeException("Failed to save employee to MongoDB: " + e.getMessage());
        }

        try {
            // Employee 객체를 스프레드시트 행으로 변환
            List<Object> row = convertEmployeeToSpreadsheetRow(employee);

            googleSheetsHelper.appendRow(spreadsheetId, range, row);
            log.info("Successfully appended data to Google Sheets for employee ID: {}", employeeId);
        } catch (Exception e) {
            log.error("Failed to append data to Google Sheets: ", e);
            throw new RuntimeException("Failed to append data to Google Sheets: " + e.getMessage());
        }

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

    private List<Object> convertEmployeeToSpreadsheetRow(Employee employee) {
        return Arrays.asList(
                employee.getEmployeeId(),                                // 사번
                employee.getPersonalInfo().getName(),                   // 이름
                employee.getPersonalInfo().getJoinDate(),               // 입사일
                employee.getPersonalInfo().getDepartment(),             // 소속
                employee.getPersonalInfo().getJobGroup(),               // 직무그룹
                employee.getPersonalInfo().getLevel(),                  // 레벨
                employee.getAccount().getUsername(),                    // 아이디
                employee.getAccount().getDefaultPassword(),             // 기본패스워드
                employee.getAccount().getUpdatedPassword(),             // 변경패스워드
                formatPoints(employee.getPoints())                      // 총경험치 등 포인트 데이터
        );
    }

    private String formatPoints(Map<String, Integer> points) {
        if (points == null || points.isEmpty()) {
            return "0";
        }
        return points.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
    }

//    private List<Object> generateExperienceData(Map<String, Integer> points) {
//        // 2023년부터 2013년까지의 연도 리스트 생성
//        List<String> years = Arrays.asList("2023년", "2022년", "2021년", "2020년", "2019년",
//                "2018년", "2017년", "2016년", "2015년", "2014년", "2013년");
//
//        // 각 연도에 대해 경험치를 채움 (없으면 0으로 대체)
//        return years.stream()
//                .map(year -> points != null && points.containsKey(year) ? points.get(year) : 0)
//                .collect(Collectors.toList());
//    }

    private Map<String, Integer> initializePoints() {
        // 2023년부터 2013년까지의 연도 리스트
        List<String> years = Arrays.asList("2023년", "2022년", "2021년", "2020년", "2019년",
                "2018년", "2017년", "2016년", "2015년", "2014년", "2013년");

        // 모든 연도에 대해 기본값 0 설정
        return years.stream()
                .collect(Collectors.toMap(year -> year, year -> 0));
    }

}