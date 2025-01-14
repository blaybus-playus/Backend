package org.example.playus.domain.sheet;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.playus.domain.board.Board;
import org.example.playus.domain.board.BoardRepositoryMongo;
import org.example.playus.domain.employee.Employee;
import org.example.playus.domain.employee.EmployeeRepositoryMongo;
import org.example.playus.domain.employee.RecentExpDetail;
import org.example.playus.domain.employeeExp.EmployeeExp;
import org.example.playus.domain.employeeExp.EmployeeExpRepository;
import org.example.playus.domain.evaluation.Evaluation;
import org.example.playus.domain.evaluation.EvaluationRepository;
import org.example.playus.domain.evaluation.PersonalEvaluation;
import org.example.playus.domain.level.Level;
import org.example.playus.domain.level.LevelRepository;
import org.example.playus.domain.project.Project;
import org.example.playus.domain.project.ProjectRepository;
import org.example.playus.domain.quest.groupGuset.GroupExperience;
import org.example.playus.domain.quest.groupGuset.GroupQuest;
import org.example.playus.domain.quest.groupGuset.GroupQuestRepositoryMongo;
import org.example.playus.domain.quest.leaderQuest.*;
import org.example.playus.global.exception.CustomException;
import org.example.playus.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleSheetService {
    private static final String APPLICATION_NAME = "Google Sheets API Example";
    private static final String CREDENTIALS_FILE_PATH = "googleSheet/google.json"; //docker 에서 서비스 계정 키경로

    private static final GoogleSheetsHelper googleSheetsHelper = new GoogleSheetsHelper();
    private static final Logger log = LoggerFactory.getLogger(GoogleSheetService.class);

    private final EmployeeRepositoryMongo employeeRepositoryMongo;
    private final GroupQuestRepositoryMongo groupQuestRepositoryMongo;
    private final LeaderQuestRepository leaderQuestRepository;
    private final BoardRepositoryMongo boardRepositoryMongo;
    private final LeaderQuestExpRepository leaderQuestExpRepository;
    private final ProjectRepository projectRepository;
    private final EvaluationRepository evaluationRepository;
    private final EmployeeExpRepository employeeExpRepository;
    private final LevelRepository levelRepository;

    // TODO : Google Sheets API를 Service layer에서 분리해야 함
    public List<Object> getSheetData(String spreadsheetId, String range) throws IOException, GeneralSecurityException {
        try {
            // GoogleCredential 생성 (InputStream을 통해 classpath 리소스 불러오기)
            InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(CREDENTIALS_FILE_PATH);
            if (resourceStream == null) {
                throw new IllegalArgumentException("Credential file not found in classpath: " + CREDENTIALS_FILE_PATH);
            }
            GoogleCredentials credentials = GoogleCredentials.fromStream(resourceStream)
                    .createScoped(List.of("https://www.googleapis.com/auth/spreadsheets"));

            // Sheets API 클라이언트 생성
            Sheets sheetsService = new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials)
            )
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // API 호출
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            return response.getValues() != null ? response.getValues().get(0) : List.of(); // 첫 번째 행 반환
        } catch (IOException e) {
            throw new RuntimeException("Error accessing Google Sheets API: " + e.getMessage());
        }
    }


    @Transactional
    public void syncAll(String spreadSheetId, String employeeRange, String groupQuestRange, String leaderQuestRange, String boardRANGE,
                        String projectRANGE, String evaluationRange, String groupEmployeeExpRange, String levelExpRange) {
        try {
            syncEmployeeData(spreadSheetId, employeeRange);
            syncGroupQuestData(spreadSheetId, groupQuestRange);
            syncLeaderQuestData(spreadSheetId, leaderQuestRange);
            syncLeaderQuestExp(spreadSheetId, leaderQuestRange);
            syncBoard(spreadSheetId, boardRANGE);
            syncProject(spreadSheetId, projectRANGE);
            syncEvaluation(spreadSheetId, evaluationRange);
            syncGroupEmployeeExp(spreadSheetId, groupEmployeeExpRange);
            syncLevelExp(spreadSheetId, levelExpRange);

        } catch (Exception e) {
            throw new RuntimeException("MongoDB 동기화 중 오류 발생: " + e.getMessage());
        }
    }

    @Transactional
    public void syncEmployeeData(String spreadsheetId, String range) throws Exception {
        try {
            List<List<Object>> sheetData = googleSheetsHelper.readSheetData(spreadsheetId, range);
            log.info("Sheet Data Read: {}", sheetData);

            List<Employee> newEmployees = GoogleSheetsConvert.convertToUsers(sheetData);

            // 기존 데이터 조회
            List<Employee> existingEmployees = employeeRepositoryMongo.findAll();

            // 중복되지 않는 새로운 데이터 필터링
            List<Employee> employeesToSave = newEmployees.stream()
                    .filter(newEmployee -> existingEmployees.stream()
                            .noneMatch(existingEmployee -> existingEmployee.getEmployeeId().equals(newEmployee.getEmployeeId())))
                    .toList();

            // 새로운 데이터 저장
            if (!employeesToSave.isEmpty()) {
                employeeRepositoryMongo.saveAll(employeesToSave);
            } else {
                log.info("추가 저장할 새로운 Employee 데이터가 없습니다.");
            }

        } catch (Exception e) {
            throw new RuntimeException("Error while syncing Google Sheets to MongoDB: " + e.getMessage());
        }
    }

    @Transactional
    public void syncGroupQuestData(String spreadSheetId, String groupQuestRange) throws Exception {

        List<GroupQuest> newGroupQuests = moduleGroupQuestData(spreadSheetId, groupQuestRange);
        //TODO : get(0)을 했을 경우 다른 값으로 저장 될 가능성을 배제하는 것이기 때문에 수정해야 할 필요가 있다.
        List<GroupQuest> existingGroupQuests = groupQuestRepositoryMongo.findAllByAffiliationAndDepartment(
                newGroupQuests.get(0).getAffiliation(), newGroupQuests.get(0).getDepartment());

        String affiliation = newGroupQuests.get(0).getAffiliation();

        // 새로운 데이터 업데이트 또는 추가
        for (GroupQuest newQuest : newGroupQuests) {
            boolean isUpdated = false;
            for (GroupQuest existingQuest : existingGroupQuests) {
                GroupExperience existingExp = existingQuest.getGroupExperiences();
                GroupExperience newExp = newQuest.getGroupExperiences();

                if (existingExp.getWeek() == newExp.getWeek()) {
                    if (existingExp.isDifferent(newExp)) {

                        existingExp.setExperience(newExp.getExperience());
                        existingExp.setEtc(newExp.getEtc());

                        groupQuestRepositoryMongo.save(existingQuest);

                        RecentExpDetail recentExpDetail = RecentExpDetail.builder()
                                .date(existingQuest.getModifiedAt())
                                .questGroup("그룹 퀘스트")
                                .questName("직무별 퀘스트")
                                .score(existingQuest.getGroupExperiences().getExperience())
                                .build();
                        // 업데이트 되는 시점의 경험치를 recentExpDetails에 저장
                        List<Employee> employees = employeeRepositoryMongo.findAllByPersonalInfo_Department(affiliation);
                        for (Employee employee : employees) {
                            List<RecentExpDetail> recentExpDetailList = employee.getRecentExpDetails();
                            recentExpDetailList.add(recentExpDetail);
                            employee.setRecentExpDetails(recentExpDetailList);
                            employeeRepositoryMongo.save(employee);
                        }
                    }
                    isUpdated = true;
                    break;
                }
            }
            if (!isUpdated) {
                groupQuestRepositoryMongo.save(newQuest);
            }
        }
    }

    @Transactional
    public void syncLeaderQuestData(String spreadSheetId, String leaderRange) {
        try {

            String affiliationRange = leaderRange + "!J8";
            String affiliation = googleSheetsHelper.readCell(spreadSheetId, affiliationRange);

            String leaderQuestRange = leaderRange + "!J10:Q13";
            List<List<Object>> leaderQuestData = googleSheetsHelper.readSheetData(spreadSheetId, leaderQuestRange);

            List<LeaderQuest> newLeaderQuests = GoogleSheetsConvert.convertToLeaderQuest(affiliation, leaderQuestData);

            // 기존 데이터 삭제
            List<LeaderQuest> existingLeaderQuests = leaderQuestRepository.findAllByAffiliation(affiliation);
            if (!existingLeaderQuests.isEmpty()) {
                leaderQuestRepository.deleteAll(existingLeaderQuests);
            }

            // 새로운 데이터 저장
            leaderQuestRepository.saveAll(newLeaderQuests);

        } catch (Exception e) {
            throw new RuntimeException("MongoDB 동기화 중 오류 발생: " + e.getMessage());
        }
    }

    @Transactional
    public void syncLeaderQuestExp(String spreadSheetId, String leaderQuestRANGE) {
        try {
            String affiliationRange = leaderQuestRANGE + "!J8";
            String affiliation = googleSheetsHelper.readCell(spreadSheetId, affiliationRange);

            String leaderQuestRange = leaderQuestRANGE + "!B9:G";
            List<List<Object>> leaderQuestExpData = googleSheetsHelper.readSheetData(spreadSheetId, leaderQuestRange);
            List<LeaderQuestExp> newLeaderQuestExpList = GoogleSheetsConvert.convertToLeaderQuestExp(affiliation, leaderQuestExpData);

            // 기존 데이터 조회
            List<LeaderQuestExp> existingLeaderQuestExps = leaderQuestExpRepository.findAllByAffiliation(affiliation);
            // 새 데이터 업데이트 또는 추가
            for (LeaderQuestExp newExp : newLeaderQuestExpList) {
                boolean isUpdated = false;
                for (LeaderQuestExp existingExp : existingLeaderQuestExps) {
                    LeaderQuestEmployeeList existingList = existingExp.getLeaderQuestEmployeeList();
                    LeaderQuestEmployeeList newList = newExp.getLeaderQuestEmployeeList();
                    if (existingList.getMonth() == newList.getMonth() &&
                            existingList.getEmployeeId() == newList.getEmployeeId() &&
                            existingList.getEmployeeName().equals(newList.getEmployeeName())) {

                        // 변경된 데이터 업데이트
                        if (existingExp.getLeaderQuestEmployeeList().isDifferent(newList)) {
                            existingList.setQuestName(newList.getQuestName());
                            existingList.setAchievement(newList.getAchievement());
                            existingList.setScore(newList.getScore());

                            leaderQuestExpRepository.save(existingExp);

                            Employee employee = employeeRepositoryMongo.findById(String.valueOf(existingList.getEmployeeId()))
                                    .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));

                            RecentExpDetail recentExpDetail = RecentExpDetail.builder()
                                    .date(existingExp.getModifiedAt())
                                    .questGroup("리더 퀘스트")
                                    .questName(existingList.getQuestName())
                                    .score(existingList.getScore())
                                    .build();
                            List<RecentExpDetail> recentExpDetailList = employee.getRecentExpDetails();

                            recentExpDetailList.add(recentExpDetail);
                            employee.setRecentExpDetails(recentExpDetailList);
                            employeeRepositoryMongo.save(employee);
                        }

                        isUpdated = true;
                        break;
                    }
                }
                if (!isUpdated) {
                    leaderQuestExpRepository.save(newExp);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("MongoDB 동기화 중 오류 발생: " + e.getMessage());
        }
    }

    // 1.구글 데이터를 읽어 mongoDB에 저장 2.mongoDB 데이터를 다시 읽어와 구글 데이터에 업데이트
    @Transactional
    public void syncBoard(String spreadsheetId, String boardRange) throws Exception {
        try {
            // Step 1: Google Sheets에서 게시글 데이터를 읽어와 DB에 저장
            List<List<Object>> boardSheetData = googleSheetsHelper.readSheetData(spreadsheetId, boardRange);
            log.info("게시글 데이터 읽기 완료: {}", boardSheetData);

            // 게시글 객체로 변환 및 저장
            List<Board> boardsFromSheet = GoogleSheetsConvert.convertToBoards(boardSheetData);

            // 기존 데이터 삭제
            boardRepositoryMongo.deleteAll();
            log.info("MongoDB의 기존 게시글 데이터 삭제 완료.");

            // 새 데이터 저장
            boardRepositoryMongo.saveAll(boardsFromSheet);
            log.info("MongoDB에 새 게시글 데이터 저장 완료.");

            // Step 2: MongoDB에서 게시글을 가져와 Google Sheets 업데이트
            List<List<Object>> updatedPostData = new ArrayList<>();
            updatedPostData.add(List.of("번호", "제목", "글", "직군"));  // 헤더 추가

            for (Board board : boardsFromSheet) {
                if (board.getTitle() != null && !board.getTitle().isEmpty()) {
                    updatedPostData.add(List.of(
                            board.getId(),  // MongoDB의 _id를 사용
                            board.getTitle(),
                            board.getContent() != null ? board.getContent() : "",
                            board.getJobGroup() != null ? board.getJobGroup().name() : ""  // 직군 enum 값 추가
                    ));
                }
            }

            // 시트 업데이트
            googleSheetsHelper.updateSheetData(spreadsheetId, boardRange, updatedPostData);

        } catch (Exception e) {
            throw new RuntimeException("게시글 동기화 중 오류 발생: " + e.getMessage());
        }
    }

    @Transactional
    public void syncProject(String spreadSheetId, String projectRANGE) {
        try {
            List<List<Object>> projectData = googleSheetsHelper.readSheetData(spreadSheetId, projectRANGE);
            List<Project> newProjects = GoogleSheetsConvert.convertToProject(projectData);

            // 기존 데이터 조회
            List<Project> existingProjects = projectRepository.findAll();

            // 중복 데이터 필터링 (새로운 데이터만 선택)
            List<Project> projectsToSave = newProjects.stream()
                    .filter(newProject -> existingProjects.stream()
                            .noneMatch(existingProject ->
                                    existingProject.getMonth() == newProject.getMonth() &&
                                            existingProject.getDay() == newProject.getDay() &&
                                            existingProject.getEmployeeId() == newProject.getEmployeeId() &&
                                            existingProject.getEmployeeName().equals(newProject.getEmployeeName()) &&
                                            existingProject.getProjectTitle().equals(newProject.getProjectTitle()) &&
                                            existingProject.getScore() == newProject.getScore()
                            )) // 중복 확인
                    .toList();

            for(Project project : projectsToSave) {
                Employee employee = employeeRepositoryMongo.findById(String.valueOf(project.getEmployeeId()))
                        .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));
                List<RecentExpDetail> recentExpDetailList = employee.getRecentExpDetails();
                RecentExpDetail recentExpDetail = RecentExpDetail.builder()
                        .date(project.getModifiedAt())
                        .questGroup("전사 프로젝트")
                        .questName(project.getProjectTitle())
                        .score(project.getScore())
                        .build();
                recentExpDetailList.add(recentExpDetail);
                employee.setRecentExpDetails(recentExpDetailList);
                employeeRepositoryMongo.save(employee);
            }

            // 새로운 데이터 저장
            if (!projectsToSave.isEmpty()) {
                projectRepository.saveAll(projectsToSave);
            } else {
                log.info("추가 저장할 새로운 Project 데이터가 없습니다.");
            }
        } catch (Exception e) {
            throw new RuntimeException("MongoDB 동기화 중 오류 발생: " + e.getMessage());
        }
    }

    @Transactional
    public void syncEvaluation(String spreadSheetId, String evaluationRANGE) {
        try {
            // 평가 데이터 읽기 및 저장을 처리하는 메서드 호출
            processEvaluationData(spreadSheetId, evaluationRANGE, "!B7", "!B9:F");
            processEvaluationData(spreadSheetId, evaluationRANGE, "!H7", "!H9:L");
        } catch (Exception e) {
            throw new RuntimeException("MongoDB 동기화 중 오류 발생: " + e.getMessage());
        }
    }

    @Transactional
    public void syncGroupEmployeeExp(String spreadSheetId, String groupEmployeeExpRange) {
        try {
            for (int year = 2022; year <= 2024; year++) {
                String yearRange = year + " " + groupEmployeeExpRange;

                String titleRange = yearRange + "!B23";
                String title = googleSheetsHelper.readCell(spreadSheetId, titleRange);

                int maxExp = Integer.parseInt(googleSheetsHelper.readCell(spreadSheetId, yearRange + "!C14"))
                        + Integer.parseInt(googleSheetsHelper.readCell(spreadSheetId, yearRange + "!E14"))
                        + Integer.parseInt(googleSheetsHelper.readCell(spreadSheetId, yearRange + "!G14"));

                String groupEmployeeExpDataRange = yearRange + "!B25:L";
                List<List<Object>> groupEmployeeExpData = googleSheetsHelper.readSheetData(spreadSheetId, groupEmployeeExpDataRange);
                List<EmployeeExp> employeeExpList = GoogleSheetsConvert.convertToEmployeeExp(title, groupEmployeeExpData);

                // 연도 필드 설정
                for (EmployeeExp employeeExp : employeeExpList) {
                    employeeExp.setYear(year);
                    employeeExp.setMaxExp(maxExp);
                }

                // 기존 데이터 삭제 (해당 연도 데이터만 삭제)
                employeeExpRepository.deleteByYear(year);

                // 새로운 데이터 저장
                employeeExpRepository.saveAll(employeeExpList);
            }
        } catch (Exception e) {
            log.error("Google Sheets 동기화 중 오류 발생: ", e);
            throw new RuntimeException("MongoDB 동기화 중 오류 발생: " + e.getMessage());
        }
    }

    @Transactional
    public void syncLevelExp(String spreadSheetId, String levelExpRange) {
        try {
            String levelF = googleSheetsHelper.readCell(spreadSheetId, levelExpRange + "!B7");
            String levelB = googleSheetsHelper.readCell(spreadSheetId, levelExpRange + "!E7");
            String levelG = googleSheetsHelper.readCell(spreadSheetId, levelExpRange + "!H7");
            String levelT = googleSheetsHelper.readCell(spreadSheetId, levelExpRange + "!K7");

            String levelFDataRange = levelExpRange + "!B8:C";
            String levelBDataRange = levelExpRange + "!E8:F";
            String levelGDataRange = levelExpRange + "!H8:I";
            String levelTDataRange = levelExpRange + "!K8:L";

            List<List<Object>> levelFData = googleSheetsHelper.readSheetData(spreadSheetId, levelFDataRange);
            List<List<Object>> levelBData = googleSheetsHelper.readSheetData(spreadSheetId, levelBDataRange);
            List<List<Object>> levelGData = googleSheetsHelper.readSheetData(spreadSheetId, levelGDataRange);
            List<List<Object>> levelTData = googleSheetsHelper.readSheetData(spreadSheetId, levelTDataRange);

            List<Level> levelFList = GoogleSheetsConvert.convertToLevelExp(levelF, levelFData);
            List<Level> levelBList = GoogleSheetsConvert.convertToLevelExp(levelB, levelBData);
            List<Level> levelGList = GoogleSheetsConvert.convertToLevelExp(levelG, levelGData);
            List<Level> levelTList = GoogleSheetsConvert.convertToLevelExp(levelT, levelTData);

            // 기존 데이터 삭제
            levelRepository.deleteAll();

            // 새로운 데이터 저장
            levelRepository.saveAll(levelFList);
            levelRepository.saveAll(levelBList);
            levelRepository.saveAll(levelGList);
            levelRepository.saveAll(levelTList);

        } catch (Exception e) {
            throw new RuntimeException("MongoDB 동기화 중 오류 발생: " + e.getMessage());
        }
    }

    private List<GroupQuest> moduleGroupQuestData(String spreadSheetId, String groupQuestRange) throws Exception {
        String groupRange = groupQuestRange + "!F10:H11";
        String expPerWeekRange = groupQuestRange + "!B13:D";
        String scoreInfo = groupQuestRange + "!B10:C11";

        List<List<Object>> groupData = googleSheetsHelper.readSheetData(spreadSheetId, groupRange);
        List<List<Object>> expPerWeekData = googleSheetsHelper.readSheetData(spreadSheetId, expPerWeekRange);
        List<List<Object>> scoreData = googleSheetsHelper.readSheetData(spreadSheetId, scoreInfo);

        List<GroupQuest> groupQuestList = GoogleSheetsConvert.convertToGroupQuest(groupData, expPerWeekData, scoreData);
        return groupQuestList;
    }

    private void processEvaluationData(String spreadSheetId, String evaluationRANGE, String termCell, String dataRange) throws Exception {
        // 평가 기간 및 데이터 읽기
        String termRange = evaluationRANGE + termCell;
        String evaluationDataRange = evaluationRANGE + dataRange;

        String term = googleSheetsHelper.readCell(spreadSheetId, termRange);
        List<List<Object>> evaluationData = googleSheetsHelper.readSheetData(spreadSheetId, evaluationDataRange);

        // 평가 데이터 변환
        List<Evaluation> evaluationList = GoogleSheetsConvert.convertToEvaluation(term, evaluationData);
        List<Evaluation> existingEvaluationList = evaluationRepository.findAllByTerm(term);

        // 중복 필터링
        List<Evaluation> evaluationToSave = evaluationList.stream()
                .filter(newEvaluation -> existingEvaluationList.stream()
                        .noneMatch(existingEvaluation ->
                                existingEvaluation.getPersonalEvaluation().getEmployeeId()
                                        == (newEvaluation.getPersonalEvaluation().getEmployeeId())))
                .toList();

        for(Evaluation evaluation : evaluationToSave) {
            Employee employee = employeeRepositoryMongo.findById(String.valueOf(evaluation.getPersonalEvaluation().getEmployeeId()))
                    .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));
            List<RecentExpDetail> recentExpDetailList = employee.getRecentExpDetails();
            RecentExpDetail recentExpDetail = RecentExpDetail.builder()
                    .date(evaluation.getModifiedAt())
                    .questGroup("인사평가")
                    .questName(evaluation.getTerm())
                    .score(evaluation.getPersonalEvaluation().getExperience())
                    .build();
            recentExpDetailList.add(recentExpDetail);
            employee.setRecentExpDetails(recentExpDetailList);
            employeeRepositoryMongo.save(employee);
        }
        // 저장
        if (!evaluationToSave.isEmpty()) {
            evaluationRepository.saveAll(evaluationToSave);
        } else {
            log.info("{} 평가 데이터 저장할 새로운 데이터가 없습니다.", term);
        }
    }
}