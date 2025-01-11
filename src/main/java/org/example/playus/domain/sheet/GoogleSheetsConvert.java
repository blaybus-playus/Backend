package org.example.playus.domain.sheet;

import org.example.playus.domain.employee.Account;
import org.example.playus.domain.employee.Employee;
import org.example.playus.domain.employee.PersonalInfo;
import org.example.playus.domain.board.Board;
import org.example.playus.domain.quest.groupGuset.*;

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

    public static List<Board> convertToBoards(List<List<Object>> sheetData) {
        List<Board> boards = new ArrayList<>();
        log.info("게시글 데이터: {}", sheetData);

        if (sheetData.isEmpty() || sheetData.size() < 2) {
            log.warn("게시글 데이터가 비어 있습니다.");
            return boards;
        }

        List<Object> headers = extractHeaders(sheetData);
        log.info("실제 시트 헤더: {}", headers);
        Map<String, Integer> headerIndexMap = createHeaderIndexMap(headers);

        if (!headerIndexMap.containsKey("번호") || !headerIndexMap.containsKey("제목") || !headerIndexMap.containsKey("글")) {
            log.error("헤더에 '번호', '제목', 또는 '글'이 없습니다.");
            return boards;
        }

        int numberIndex = headerIndexMap.get("번호");
        int titleIndex = headerIndexMap.get("제목");
        int contentIndex = headerIndexMap.get("글");

        for (int i = 1; i < sheetData.size(); i++) {
            List<Object> row = sheetData.get(i);

            while (row.size() <= contentIndex) {
                row.add("");  // 누락된 데이터는 빈 문자열로 채웁니다.
            }

            String id = row.size() > numberIndex ? row.get(numberIndex).toString().trim() : null;
            String title = row.size() > titleIndex ? row.get(titleIndex).toString().trim() : "";
            String content = row.size() > contentIndex ? row.get(contentIndex).toString().trim() : "";

            if (title.isBlank() || content.isBlank()) {
                log.warn("제목 또는 내용이 비어 있습니다. (행 번호: {}), 데이터: {}", i + 1, row);
            }

            Board board = new Board();
            board.setId(id);
            board.setTitle(title);
            board.setContent(content);
            boards.add(board);
        }

        return boards;
    }

    public static List<List<Object>> convertToSheetFormat(List<Board> boards) {
        List<List<Object>> sheetData = new ArrayList<>();

        // 헤더 행 추가
        sheetData.add(List.of("제목", "글"));

        // 게시글 데이터 행 추가
        for (Board board : boards) {
            List<Object> row = List.of(
                    board.getTitle(),
                    board.getContent()
            );
            sheetData.add(row);
        }
        return sheetData;
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

