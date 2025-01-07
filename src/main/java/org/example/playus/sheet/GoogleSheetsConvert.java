package org.example.playus.sheet;

import java.util.*;

public class GoogleSheetsConvert {

    public static List<User> convertToUsers(List<List<Object>> sheetData) {
        List<User> users = new ArrayList<>();

        if (sheetData.isEmpty()) return users;

        // 첫 번째 줄에서 헤더 정보를 추출
        List<Object> headers = sheetData.get(0);
        Map<String, Integer> headerIndexMap = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            headerIndexMap.put(headers.get(i).toString(), i);
        }

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
            if (row.size() < headers.size()) continue;

            // User 객체 생성
            User user = new User();
            user.setEmployeeId(row.get(headerIndexMap.get("사번")).toString());

            // PersonalInfo 생성 및 설정
            PersonalInfo personalInfo = new PersonalInfo();
            personalInfo.setName(row.get(headerIndexMap.get("이름")).toString());
            personalInfo.setJoinDate(row.get(headerIndexMap.get("입사일")).toString());
            personalInfo.setDepartment(row.get(headerIndexMap.get("소속")).toString());
            personalInfo.setJobGroup(row.get(headerIndexMap.get("직무그룹")).toString());
            personalInfo.setLevel(row.get(headerIndexMap.get("레벨")).toString());
            user.setPersonalInfo(personalInfo);

            // Account 생성 및 설정
            Account account = new Account();
            account.setUsername(row.get(headerIndexMap.get("아이디")).toString());
            account.setDefaultPassword(row.get(headerIndexMap.get("기본패스워드")).toString());
            account.setUpdatedPassword(row.get(headerIndexMap.get("변경패스워드")) == null ? null :
                    row.get(headerIndexMap.get("변경패스워드")).toString());
            user.setAccount(account);

            // 연도별 포인트 설정
            Map<String, Integer> points = new HashMap<>();
            for (int year = 2023; year >= 2013; year--) {
                String yearKey = year + "년";
                if (headerIndexMap.containsKey(yearKey)) {
                    String pointString = row.get(headerIndexMap.get(yearKey)).toString().replace(",", "").trim();
                    int point = pointString.isEmpty() ? 0 : Integer.parseInt(pointString);
                    points.put(String.valueOf(year), point);
                }
            }
            user.setPoints(points);

            users.add(user);
        }
        return users;
    }
}
