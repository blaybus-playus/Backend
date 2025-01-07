package org.example.playus.domain.sheet;

import org.example.playus.domain.employee.Account;
import org.example.playus.domain.employee.Employee;
import org.example.playus.domain.employee.PersonalInfo;

import java.util.*;

public class GoogleSheetsConvert {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GoogleSheetsConvert.class);

    public static List<Employee> convertToUsers(List<List<Object>> sheetData) {
        List<Employee> employees = new ArrayList<>();

        if (sheetData.isEmpty()) {
            log.warn("Sheet data is empty.");
            return employees;
        }

        // 첫 번째 줄에서 헤더 정보를 추출
        List<Object> headers = sheetData.get(0);
        Map<String, Integer> headerIndexMap = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            headerIndexMap.put(headers.get(i).toString(), i);
        }

        // 헤더 확인
        log.info("Headers: {}", headers);
        log.info("Header Index Map: {}", headerIndexMap);

        // 필수 헤더 이름 정의
        String[] requiredHeaders = {
                "사번", "이름", "입사일", "소속", "직무그룹", "레벨",
                "아이디", "기본패스워드", "변경패스워드",
                "2023년", "2022년", "2021년", "2020년", "2019년", "2018년",
                "2017년", "2016년", "2015년", "2014년", "2013년"
        };

        // 필요한 헤더가 모두 있는지 확인
        for (String header : requiredHeaders) {
            if (!headerIndexMap.containsKey(header)) {
                throw new IllegalArgumentException("헤더에 '" + header + "' 열이 없습니다.");
            }
        }

        // 데이터 행 처리
        for (int i = 1; i < sheetData.size(); i++) { // 첫 번째 줄은 헤더로 간주
            List<Object> row = sheetData.get(i);

            // 데이터가 누락되지 않았는지 확인
            if (row.size() < headers.size()) {
                log.warn("Skipping row {}: Missing data (Expected size: {}, Actual size: {})", i, headers.size(), row.size());
                continue;
            }

            log.info("Processing row {}: {}", i, row);

            // User 객체 생성
            Employee employee = new Employee();
            employee.setEmployeeId(row.get(headerIndexMap.get("사번")).toString());

            // PersonalInfo 생성 및 설정
            PersonalInfo personalInfo = new PersonalInfo();
            personalInfo.setName(row.get(headerIndexMap.get("이름")).toString());
            personalInfo.setJoinDate(row.get(headerIndexMap.get("입사일")).toString());
            personalInfo.setDepartment(row.get(headerIndexMap.get("소속")).toString());
            personalInfo.setJobGroup(row.get(headerIndexMap.get("직무그룹")).toString());
            personalInfo.setLevel(row.get(headerIndexMap.get("레벨")).toString());
            employee.setPersonalInfo(personalInfo);

            // Account 생성 및 설정
            Account account = Account.builder()
                    .username(row.get(headerIndexMap.get("아이디")).toString())
                    .defaultPassword(row.get(headerIndexMap.get("기본패스워드")).toString())
                    .updatedPassword(row.get(headerIndexMap.get("변경패스워드")) == null ? null :
                            row.get(headerIndexMap.get("변경패스워드")).toString())
                    .build();
            employee.setAccount(account);

            // 연도별 포인트 설정
            Map<String, Integer> points = new HashMap<>();
            for (int year = 2023; year >= 2013; year--) {
                String yearKey = year + "년";
                if (headerIndexMap.containsKey(yearKey)) {
                    String pointString = row.get(headerIndexMap.get(yearKey)).toString().replace(",", "").trim();

                    // 빈 값 처리: 빈 값이면 0으로 처리
                    if (pointString.isEmpty()) {
                        pointString = "0"; // 빈 값인 경우 0으로 처리
                    }

                    int point = Integer.parseInt(pointString);
                    points.put(String.valueOf(year), point);
                } else {
                    // 연도 열이 없을 경우 0으로 처리
                    points.put(String.valueOf(year), 0);
                }
            }
            employee.setPoints(points);

            log.info("Employee created: {}", employee);

            employees.add(employee);
        }

        log.info("Total employees processed: {}", employees.size());
        return employees;
    }
}
