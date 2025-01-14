package org.example.playus.domain.signup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.playus.domain.admin.Admin;
import org.example.playus.domain.admin.Role;
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


import java.util.ArrayList;
import java.util.Arrays;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
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

    private final String range = "구성원정보!B10:V";

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
        if (!isValidDateFormat(joinDate)) {
            throw new CustomException(ErrorCode.BAD_REQUEST);  // 잘못된 요청 예외
        }

        String employeeId;
        try {
            employeeId = generateEmployeeId(joinDate);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);  // 서버 내부 오류 예외
        }

        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);
        employee.setAccount(account);
        employee.setPersonalInfo(requestDto.getPersonalInfo());
        employee.setPoints(initializePoints());

        // 기본 권한 설정
        Admin admin = new Admin(Role.ROLE_USER);  // 기본적으로 일반 사용자로 설정

        // "Admin"이라는 값이 PersonalInfo의 department에 입력되면 관리자 권한 설정
        if ("Admin".equalsIgnoreCase(requestDto.getPersonalInfo().getDepartment())) {
            admin = new Admin(Role.ROLE_ADMIN);  // 관리자 권한 부여
        }

        employee.setAdmin(admin);

        try {
            employeeRepositoryMongo.save(employee);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.EMPLOYEE_EXIST);  // 사번 중복 저장 오류
        }

        try {
            // Employee 객체를 스프레드시트 행으로 변환
            List<Object> row = convertEmployeeToSpreadsheetRow(employee);
            googleSheetsHelper.appendRow(spreadsheetId, range, row);
        } catch (Exception e) {
            throw new RuntimeException("Failed to append data to Google Sheets: " + e.getMessage());
        } // Errorcode enum 생성 

        return SignupResponseDto.builder()
                .success(true)
                .message("회원가입이 완료되었습니다.")
                .build();
    }

    private String generateEmployeeId(String joinDate) {
        // `joinDate`를 "yyyyMMdd" 형식으로 변환
        String formattedDate = joinDate.replace("-", "");  // 예: 2025-01-01 → 20250101

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

        // 최종 사번 반환 (예: 2025010101)
        return formattedDate + formattedNumber;
    }

    private List<Object> convertEmployeeToSpreadsheetRow(Employee employee) {
        List<Object> row = new ArrayList<>();

        // 기본 정보 추가
        row.add(employee.getEmployeeId());                              // 사번
        row.add(employee.getPersonalInfo().getName());                 // 이름
        row.add(employee.getPersonalInfo().getJoinDate());             // 입사일
        row.add(employee.getPersonalInfo().getDepartment());           // 소속
        row.add(employee.getPersonalInfo().getJobGroup());             // 직무그룹
        row.add(employee.getPersonalInfo().getLevel());                // 레벨
        row.add(employee.getAccount().getUsername());                  // 아이디
        row.add(employee.getAccount().getDefaultPassword());           // 기본패스워드
        row.add(employee.getAccount().getUpdatedPassword());           // 변경패스워드

        // 총경험치 계산
        int totalPoints = employee.getPoints().values().stream().mapToInt(Integer::intValue).sum();
        row.add(String.format("%,d", totalPoints));                    // 총경험치 (콤마 포함 형식)

        // 연도별 경험치 추가
        row.addAll(generateExperienceData(employee.getPoints()));      // 연도별 경험치

        return row;
    }

    private String formatPoints(Map<String, Integer> points) {
        if (points == null || points.isEmpty()) {
            return "0";
        }
        return points.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
    }

    private List<Object> generateExperienceData(Map<String, Integer> points) {
        List<String> years = Arrays.asList("2023년", "2022년", "2021년", "2020년", "2019년",
                "2018년", "2017년", "2016년", "2015년", "2014년", "2013년");

        // 각 연도별 경험치 반환 (없으면 0으로 대체)
        return years.stream()
                .map(year -> points != null && points.containsKey(year) ? points.get(year) : 0)
                .collect(Collectors.toList());
    }


    private Map<String, Integer> initializePoints() {
        // 2023년부터 2013년까지의 연도 리스트
        List<String> years = Arrays.asList("2023년", "2022년", "2021년", "2020년", "2019년",
                "2018년", "2017년", "2016년", "2015년", "2014년", "2013년");

        // 모든 연도에 대해 기본값 0 설정
        return years.stream()
                .collect(Collectors.toMap(year -> year, year -> 0));
    }

    private boolean isValidDateFormat(String date) {
        try {
            LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
