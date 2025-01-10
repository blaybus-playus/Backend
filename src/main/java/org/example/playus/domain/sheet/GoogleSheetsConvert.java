package org.example.playus.domain.sheet;

import org.example.playus.domain.employee.Account;
import org.example.playus.domain.employee.Employee;
import org.example.playus.domain.employee.PersonalInfo;
import org.example.playus.domain.quest.groupGuset.*;
import org.example.playus.domain.quest.leaderQuest.LeaderQuest;
import org.example.playus.domain.quest.leaderQuest.LeaderQuestEmployeeList;
import org.example.playus.domain.quest.leaderQuest.LeaderQuestExp;
import org.example.playus.domain.quest.leaderQuest.LeaderQuestList;

import java.util.*;

public class GoogleSheetsConvert {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GoogleSheetsConvert.class);

    // TODO : 코드 수정 필요 (너무 복잠함)
    public static List<Employee> convertToUsers(List<List<Object>> sheetData) {
        List<Employee> employees = new ArrayList<>();
        log.info("Sheet data: {}", sheetData);

        // 헤더 추출 및 헤더 맵 생성
        List<Object> headers = extractHeaders(sheetData);
        if (headers.isEmpty()) {
            return employees; // 빈 데이터 반환
        }
        Map<String, Integer> headerIndexMap = createHeaderIndexMap(headers);

        // 필수 헤더 이름 정의
        String[] requiredHeaders = {
                "사번", "이름", "입사일", "소속", "직무그룹", "레벨",
                "아이디", "기본패스워드", "변경패스워드",
                "2023년", "2022년", "2021년", "2020년", "2019년", "2018년",
                "2017년", "2016년", "2015년", "2014년", "2013년"
        };

        // 데이터 행 처리
        for (int i = 1; i < sheetData.size(); i++) { // 첫 번째 줄은 헤더로 간주
            List<Object> row = sheetData.get(i);


            // 데이터가 누락되지 않았는지 확인
//            if (row.size() < headers.size()) {
//                log.warn("Skipping row {}: Missing data (Expected size: {}, Actual size: {})", i, headers.size(), row.size());
//                continue;
//            }
//
            if (row.isEmpty() || row.stream().allMatch(cell -> cell.toString().isBlank())) {
                log.warn("Row {} is empty and will be skipped.", i);
                continue;
            }
            while (row.size() < headers.size()) {
                row.add("");  // 문자열 기본값으로 빈 문자열, 숫자는 "0"
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

            employees.add(employee);
        }
        return employees;
    }

    public static List<GroupQuest> convertToGroupQuest(List<List<Object>> groupData, List<List<Object>> expPerWeekData, List<List<Object>> scoreData) {
        List<GroupQuest> groupQuests = new ArrayList<>();

        // 헤더 추출
        Map<String, Integer> groupHeaderIndexMap = createHeaderIndexMap(extractHeaders(groupData));
        Map<String, Integer> expPerWeekHeaderIndexMap = createHeaderIndexMap(extractHeaders(expPerWeekData));
        Map<String, Integer> scoreHeaderIndexMap = createHeaderIndexMap(extractHeaders(scoreData));

        // GroupData에서 기본 정보 추출
        String affiliation = groupData.get(1).get(groupHeaderIndexMap.get("소속")).toString();
        int department = Integer.parseInt(groupData.get(1).get(groupHeaderIndexMap.get("직무그룹")).toString());
        String period = groupData.get(1).get(groupHeaderIndexMap.get("주기")).toString();
        int maxScore = Integer.parseInt(scoreData.get(1).get(scoreHeaderIndexMap.get("MAX 점수")).toString());
        int mediumScore = Integer.parseInt(scoreData.get(1).get(scoreHeaderIndexMap.get("MEDIUM 점수")).toString());

        // 모든 주차별 경험치 정보 생성
        List<GroupExperience> groupExperiences = new ArrayList<>();
        for (int i = 1; i < expPerWeekData.size(); i++) {
            List<Object> expRow = expPerWeekData.get(i);
            GroupExperience experience = GroupExperience.builder()
                    .week(Integer.parseInt(expRow.get(expPerWeekHeaderIndexMap.get("주차")).toString()))
                    .experience(expRow.get(expPerWeekHeaderIndexMap.get("부여 경험치")) != null
                            ? Integer.parseInt(expRow.get(expPerWeekHeaderIndexMap.get("부여 경험치")).toString()) : 0)
                    .etc(expRow.get(expPerWeekHeaderIndexMap.get("비고")) == null
                            ? "" : expRow.get(expPerWeekHeaderIndexMap.get("비고")).toString())
                    .build();
            groupExperiences.add(experience);
        }

        // GroupQuest 생성
        GroupQuest groupQuest = GroupQuest.builder()
                .affiliation(affiliation)
                .department(department)
                .period(period)
                .maxScore(maxScore)
                .mediumScore(mediumScore)
                .groupExperiences(groupExperiences)
                .build();

        groupQuests.add(groupQuest);
        return groupQuests;
    }

    private static List<LeaderQuest> convertToLeaderQuest(Object affiliationData, List<List<Object>> leaderQuestListData) {
        List<LeaderQuest> leaderQuests = new ArrayList<>();

        // 소속 정보 추출
        String affiliation = affiliationData.toString();

        // 헤더 추출
        Map<String, Integer> leaderQuestHeaderIndexMap = createHeaderIndexMap(extractHeaders(leaderQuestListData));

        // 리더 퀘스트 목록 생성
        for (int i = 1; i < leaderQuestListData.size(); i++) {
            List<Object> leaderQuestRow = leaderQuestListData.get(i);
            LeaderQuest leaderQuest = LeaderQuest.builder()
                    .affiliation(affiliation)
                    .leaderQuestList(LeaderQuestList.builder()
                            .id(affiliation + "-" + i)
                            .questName(leaderQuestRow.get(leaderQuestHeaderIndexMap.get("퀘스트명")).toString())
                            .period(leaderQuestRow.get(leaderQuestHeaderIndexMap.get("획득주기")).toString())
                            .totalScore(Integer.parseInt(leaderQuestRow.get(leaderQuestHeaderIndexMap.get("경험치")).toString()))
                            .maxScore(Integer.parseInt(leaderQuestRow.get(leaderQuestHeaderIndexMap.get("Max")).toString()))
                            .mediumScore(Integer.parseInt(leaderQuestRow.get(leaderQuestHeaderIndexMap.get("Median")).toString()))
                            .requireForMax(leaderQuestRow.get(leaderQuestHeaderIndexMap.get("Max조건")).toString())
                            .requireForMedium(leaderQuestRow.get(leaderQuestHeaderIndexMap.get("Median조건")).toString())
                            .build())
                    .build();
            leaderQuests.add(leaderQuest);
        }

        return leaderQuests;
    }

    private static List<LeaderQuestExp> convertToLeaderQuestExp(Object affiliation, List<List<Object>> leaderQuestExpData) {
        List<LeaderQuestExp> leaderQuestExps = new ArrayList<>();

        // 헤더 추출
        Map<String, Integer> leaderQuestExpHeaderIndexMap = createHeaderIndexMap(extractHeaders(leaderQuestExpData));

        // 리더 퀘스트 경험치 생성
        for (int i = 1; i < leaderQuestExpData.size(); i++) {
            List<Object> leaderQuestExpRow = leaderQuestExpData.get(i);
            LeaderQuestExp leaderQuestExp = LeaderQuestExp.builder()
                    .affiliation(affiliation.toString())
                    .leaderQuestEmployeeList(LeaderQuestEmployeeList.builder()
                            .month(Integer.parseInt(leaderQuestExpRow.get(leaderQuestExpHeaderIndexMap.get("월")).toString()))
                            .employeeId(Integer.parseInt(leaderQuestExpRow.get(leaderQuestExpHeaderIndexMap.get("사번")).toString()))
                            .employeeName(leaderQuestExpRow.get(leaderQuestExpHeaderIndexMap.get("대상자")).toString())
                            .questName(leaderQuestExpRow.get(leaderQuestExpHeaderIndexMap.get("리더 부여 퀘스트명")).toString())
                            .achievement(leaderQuestExpRow.get(leaderQuestExpHeaderIndexMap.get("달성내용")).toString())
                            .score(Integer.parseInt(leaderQuestExpRow.get(leaderQuestExpHeaderIndexMap.get("부여 경험치")).toString()))
                            .build())
                    .build();
            leaderQuestExps.add(leaderQuestExp);
        }

        return leaderQuestExps;
    }

    // SheetData Empty 확인 및 헤더 추출
    private static List<Object> extractHeaders(List<List<Object>> sheetData) {
        if (sheetData.isEmpty()) {
            log.warn("Sheet data is empty.");
            return Collections.emptyList();
        }
        return sheetData.get(0); // 첫 번째 줄 반환
    }

    // 헤더 맵 생성
    private static Map<String, Integer> createHeaderIndexMap(List<Object> headers) {
        Map<String, Integer> headerIndexMap = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            headerIndexMap.put(headers.get(i).toString(), i);
        }
        return headerIndexMap;
    }
}

