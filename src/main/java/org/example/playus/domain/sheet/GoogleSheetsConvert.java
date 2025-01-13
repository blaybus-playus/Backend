package org.example.playus.domain.sheet;

import org.example.playus.domain.employeeExp.EmployeeExp;
import org.example.playus.domain.employeeExp.ExpForYear;
import org.example.playus.domain.employee.Account;
import org.example.playus.domain.employee.Employee;
import org.example.playus.domain.employee.PersonalInfo;
import org.example.playus.domain.board.Board;
import org.example.playus.domain.evaluation.Evaluation;
import org.example.playus.domain.evaluation.PersonalEvaluation;
import org.example.playus.domain.level.Level;
import org.example.playus.domain.level.LevelExp;
import org.example.playus.domain.project.Project;
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
            employee.setCharacterId("man1");

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

    public static List<LeaderQuest> convertToLeaderQuest(String affiliation, List<List<Object>> leaderQuestListData) {
        List<LeaderQuest> leaderQuests = new ArrayList<>();

        // 헤더 추출
        Map<String, Integer> leaderQuestHeaderIndexMap = createHeaderIndexMap(extractHeaders(leaderQuestListData));

        // 리더 퀘스트 목록 생성
        for (int i = 1; i < leaderQuestListData.size(); i++) {
            List<Object> leaderQuestRow = leaderQuestListData.get(i);

            // 비어있는 값을 빈 문자열로 설정
            while (leaderQuestRow.size() < leaderQuestHeaderIndexMap.size()) {
                leaderQuestRow.add("");
            }

            LeaderQuest leaderQuest = LeaderQuest.builder()
                    .affiliation(affiliation)
                    .leaderQuestList(LeaderQuestList.builder()
                            .id(affiliation + "-" + i)
                            .questName(leaderQuestRow.get(leaderQuestHeaderIndexMap.get("퀘스트명")) == null
                                    ? "" : leaderQuestRow.get(leaderQuestHeaderIndexMap.get("퀘스트명")).toString())
                            .period(leaderQuestRow.get(leaderQuestHeaderIndexMap.get("획득주기")) == null
                                    ? "" : leaderQuestRow.get(leaderQuestHeaderIndexMap.get("획득주기")).toString())
                            .totalScore(leaderQuestRow.get(leaderQuestHeaderIndexMap.get("경험치")) == null
                                    ? "" : leaderQuestRow.get(leaderQuestHeaderIndexMap.get("경험치")).toString())
                            .maxScore(leaderQuestRow.get(leaderQuestHeaderIndexMap.get("Max")) != null && !leaderQuestRow.get(leaderQuestHeaderIndexMap.get("Max")).toString().isEmpty()
                                    ? Integer.parseInt(leaderQuestRow.get(leaderQuestHeaderIndexMap.get("Max")).toString()) : 0)
                            .mediumScore(leaderQuestRow.get(leaderQuestHeaderIndexMap.get("Median")) != null && !leaderQuestRow.get(leaderQuestHeaderIndexMap.get("Median")).toString().isEmpty()
                                    ? Integer.parseInt(leaderQuestRow.get(leaderQuestHeaderIndexMap.get("Median")).toString()) : 0)
                            .requireForMax(leaderQuestRow.get(leaderQuestHeaderIndexMap.get("Max조건")) == null
                                    ? "" : leaderQuestRow.get(leaderQuestHeaderIndexMap.get("Max조건")).toString())
                            .requireForMedium(leaderQuestRow.get(leaderQuestHeaderIndexMap.get("Median조건")) == null
                                    ? "" : leaderQuestRow.get(leaderQuestHeaderIndexMap.get("Median조건")).toString())
                            .build())
                    .build();
            leaderQuests.add(leaderQuest);
        }

        return leaderQuests;
    }

    public static List<LeaderQuestExp> convertToLeaderQuestExp(Object affiliation, List<List<Object>> leaderQuestExpData) {
        List<LeaderQuestExp> leaderQuestExps = new ArrayList<>();

        // 헤더 추출
        Map<String, Integer> leaderQuestExpHeaderIndexMap = createHeaderIndexMap(extractHeaders(leaderQuestExpData));

        // 리더 퀘스트 경험치 생성
        for (int i = 1; i < leaderQuestExpData.size(); i++) {
            List<Object> leaderQuestExpRow = leaderQuestExpData.get(i);
            LeaderQuestExp leaderQuestExp = LeaderQuestExp.builder()
                    .affiliation(affiliation.toString())
                    .leaderQuestEmployeeList(LeaderQuestEmployeeList.builder()
                            .month(leaderQuestExpRow.get(leaderQuestExpHeaderIndexMap.get("월")) != null && !leaderQuestExpRow.get(leaderQuestExpHeaderIndexMap.get("월")).toString().isEmpty()
                                    ? Integer.parseInt(leaderQuestExpRow.get(leaderQuestExpHeaderIndexMap.get("월")).toString()) : 0)
                            .employeeId(leaderQuestExpRow.get(leaderQuestExpHeaderIndexMap.get("사번"))!= null && !leaderQuestExpRow.get(leaderQuestExpHeaderIndexMap.get("사번")).toString().isEmpty()
                                            ? Integer.parseInt(leaderQuestExpRow.get(leaderQuestExpHeaderIndexMap.get("사번")).toString()) : 0)
                            .employeeName(leaderQuestExpRow.get(leaderQuestExpHeaderIndexMap.get("대상자")) == null ? "" : leaderQuestExpRow.get(leaderQuestExpHeaderIndexMap.get("대상자")).toString())
                            .questName(leaderQuestExpRow.get(leaderQuestExpHeaderIndexMap.get("리더 부여 퀘스트명")) == null ? "" : leaderQuestExpRow.get(leaderQuestExpHeaderIndexMap.get("리더 부여 퀘스트명")).toString())
                            .achievement(leaderQuestExpRow.get(leaderQuestExpHeaderIndexMap.get("달성내용"))== null ? "" : leaderQuestExpRow.get(leaderQuestExpHeaderIndexMap.get("달성내용")).toString())
                            .score(leaderQuestExpRow.get(leaderQuestExpHeaderIndexMap.get("부여 경험치"))!= null && !leaderQuestExpRow.get(leaderQuestExpHeaderIndexMap.get("부여 경험치")).toString().isEmpty()
                                    ? Integer.parseInt(leaderQuestExpRow.get(leaderQuestExpHeaderIndexMap.get("부여 경험치")).toString()) : 0)
                            .build())
                    .build();
            leaderQuestExps.add(leaderQuestExp);
        }

        return leaderQuestExps;
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

    public static List<Project> convertToProject(List<List<Object>> projectData) {
        List<Project> projects = new ArrayList<>();

        Map<String, Integer> projectHeaderIndexMap = createHeaderIndexMap(extractHeaders(projectData));

        for (int i = 1; i < projectData.size(); i++) {
            List<Object> projectRow = projectData.get(i);
            Project project = Project.builder()
                    .month(projectRow.get(projectHeaderIndexMap.get("월")) != null && !projectRow.get(projectHeaderIndexMap.get("월")).toString().isEmpty()
                            ? Integer.parseInt(projectRow.get(projectHeaderIndexMap.get("월")).toString()) : 0)
                    .day(projectRow.get(projectHeaderIndexMap.get("일")) != null && !projectRow.get(projectHeaderIndexMap.get("일")).toString().isEmpty()
                            ? Integer.parseInt(projectRow.get(projectHeaderIndexMap.get("일")).toString()) : 0)
                    .employeeId(projectRow.get(projectHeaderIndexMap.get("사번")) != null && !projectRow.get(projectHeaderIndexMap.get("사번")).toString().isEmpty()
                            ? Integer.parseInt(projectRow.get(projectHeaderIndexMap.get("사번")).toString()) : 0)
                    .employeeName(projectRow.get(projectHeaderIndexMap.get("대상자")) == null ? "" : projectRow.get(projectHeaderIndexMap.get("대상자")).toString())
                    .projectTitle(projectRow.get(projectHeaderIndexMap.get("전사 프로젝트명")) == null ? "" : projectRow.get(projectHeaderIndexMap.get("전사 프로젝트명")).toString())
                    .score(projectRow.get(projectHeaderIndexMap.get("부여 경험치")) != null && !projectRow.get(projectHeaderIndexMap.get("부여 경험치")).toString().isEmpty()
                            ? Integer.parseInt(projectRow.get(projectHeaderIndexMap.get("부여 경험치")).toString()) : 0)
                    .build();

            projects.add(project);
        }

        return projects;
    }

    public static List<Evaluation> convertToEvaluation(String term, List<List<Object>> evaluationData) {
        List<Evaluation> evaluations = new ArrayList<>();

        Map<String, Integer> evaluationHeaderIndexMap = createHeaderIndexMap(extractHeaders(evaluationData));

        List<PersonalEvaluation> personalEvaluations = new ArrayList<>();
        for (int i = 1; i < evaluationData.size(); i++) {
            List<Object> evaluationRow = evaluationData.get(i);

            PersonalEvaluation personalEvaluation = PersonalEvaluation.builder()
                    .employeeId(evaluationRow.get(evaluationHeaderIndexMap.get("사번")) != null && !evaluationRow.get(evaluationHeaderIndexMap.get("사번")).toString().isEmpty()
                            ? Integer.parseInt(evaluationRow.get(evaluationHeaderIndexMap.get("사번")).toString()) : 0)
                    .name(evaluationRow.get(evaluationHeaderIndexMap.get("대상자")) == null ? "" : evaluationRow.get(evaluationHeaderIndexMap.get("대상자")).toString())
                    .grade(evaluationRow.get(evaluationHeaderIndexMap.get("인사평가 등급")) == null ? "" : evaluationRow.get(evaluationHeaderIndexMap.get("인사평가 등급")).toString())
                    .experience(evaluationRow.get(evaluationHeaderIndexMap.get("부여 경험치")) != null && !evaluationRow.get(evaluationHeaderIndexMap.get("부여 경험치")).toString().isEmpty()
                            ? Integer.parseInt(evaluationRow.get(evaluationHeaderIndexMap.get("부여 경험치")).toString()) : 0)
                    .note(evaluationRow.get(evaluationHeaderIndexMap.get("비고")) == null ? "-" : evaluationRow.get(evaluationHeaderIndexMap.get("비고")).toString())
                    .build();

            personalEvaluations.add(personalEvaluation);
        }
        Evaluation evaluation = Evaluation.builder()
                .term(term)
                .personalEvaluation(personalEvaluations)
                .build();
        evaluations.add(evaluation);

        return evaluations;
    }

    public static List<EmployeeExp> convertToEmployeeExp(String title, List<List<Object>> groupEmployeeExpData) {

        List<EmployeeExp> employeeExps = new ArrayList<>();

        Map<String, Integer> groupEmployeeExpHeaderIndexMap = createHeaderIndexMap(extractHeaders(groupEmployeeExpData));

        for (int i = 1; i < groupEmployeeExpData.size(); i++) {
            List<Object> groupEmployeeExpRow = groupEmployeeExpData.get(i);
            ExpForYear expForYear = ExpForYear.builder()
                    .totalExp(groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("2024년 획득한 총 경험치")) != null && !groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("2024년 획득한 총 경험치")).toString().isEmpty()
                            ? Integer.parseInt(groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("2024년 획득한 총 경험치")).toString()) : 0)
                    .firstHalfEvaluationExp(groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("상반기 인사평가")) != null && !groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("상반기 인사평가")).toString().isEmpty()
                            ? Integer.parseInt(groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("상반기 인사평가")).toString()) : 0)
                    .secondHalfEvaluationExp(groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("하반기 인사평가")) != null && !groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("하반기 인사평가")).toString().isEmpty()
                            ? Integer.parseInt(groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("하반기 인사평가")).toString()) : 0)
                    .groupQuestExp(groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("직무별 퀘스트")) != null && !groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("직무별 퀘스트")).toString().isEmpty()
                            ? Integer.parseInt(groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("직무별 퀘스트")).toString()) : 0)
                    .leaderQuestExp(groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("리더부여 퀘스트")) != null && !groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("리더부여 퀘스트")).toString().isEmpty()
                            ? Integer.parseInt(groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("리더부여 퀘스트")).toString()) : 0)
                    .projectExp(groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("전사 프로젝트")) != null && !groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("전사 프로젝트")).toString().isEmpty()
                            ? Integer.parseInt(groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("전사 프로젝트")).toString()) : 0)
                    .build();

            EmployeeExp employeeExp = EmployeeExp.builder()
                    .title(title)
                    .employeeId(groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("사번")) != null && !groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("사번")).toString().isEmpty()
                            ? Integer.parseInt(groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("사번")).toString()) : 0)
                    .name(groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("이름")) == null ? "" : groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("이름")).toString())
                    .affiliation(groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("소속")) == null ? "" : groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("소속")).toString())
                    .department(groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("직무그룹")) != null && !groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("직무그룹")).toString().isEmpty()
                            ? Integer.parseInt(groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("직무그룹")).toString()) : 0)
                    .level(groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("레벨")) == null ? "" : groupEmployeeExpRow.get(groupEmployeeExpHeaderIndexMap.get("레벨")).toString())
                    .expForYear(expForYear)
                    .build();
            employeeExps.add(employeeExp);
            }
        return employeeExps;
    }


    public static List<Level> convertToLevelExp(String levelGroup, List<List<Object>> levelData) {
        List<Level> levels = new ArrayList<>();
        List<LevelExp> levelExps = new ArrayList<>();

        Map<String, Integer> levelHeaderIndexMap = createHeaderIndexMap(extractHeaders(levelData));

        for (int i = 1; i < levelData.size(); i++) {
            List<Object> levelRow = levelData.get(i);
            LevelExp levelExp = LevelExp.builder()
                    .level(levelRow.get(levelHeaderIndexMap.get("레벨")) == null ? "" : levelRow.get(levelHeaderIndexMap.get("레벨")).toString())
                    .exp(levelHeaderIndexMap.get("총 필요 경험치") != null && !levelRow.get(levelHeaderIndexMap.get("총 필요 경험치")).toString().isEmpty()
                            ? Integer.parseInt(levelRow.get(levelHeaderIndexMap.get("총 필요 경험치")).toString()) : 0)
                    .build();
            levelExps.add(levelExp);
        }
        Level level = Level.builder()
                .levelGroup(levelGroup)
                .levelExp(levelExps)
                .build();
        levels.add(level);

        return levels;
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

