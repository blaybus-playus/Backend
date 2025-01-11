package org.example.playus.domain.sheet;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.playus.domain.board.Board;
import org.example.playus.domain.board.BoardRepositoryMongo;
import org.example.playus.domain.employee.Employee;
import org.example.playus.domain.employee.EmployeeRepositoryMongo;
import org.example.playus.domain.evaluation.Evaluation;
import org.example.playus.domain.evaluation.EvaluationRepository;
import org.example.playus.domain.evaluation.PersonalEvaluation;
import org.example.playus.domain.project.Project;
import org.example.playus.domain.project.ProjectRepository;
import org.example.playus.domain.quest.groupGuset.GroupQuest;
import org.example.playus.domain.quest.groupGuset.GroupQuestRepositoryMongo;
import org.example.playus.domain.quest.leaderQuest.LeaderQuest;
import org.example.playus.domain.quest.leaderQuest.LeaderQuestExp;
import org.example.playus.domain.quest.leaderQuest.LeaderQuestExpRepository;
import org.example.playus.domain.quest.leaderQuest.LeaderQuestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleSheetService {
    private static final String APPLICATION_NAME = "Google Sheets API Example";
    private static final String CREDENTIALS_FILE_PATH = "src/main/resources/googleSheet/google.json"; // 서비스 계정 키 경로

    private static final GoogleSheetsHelper googleSheetsHelper = new GoogleSheetsHelper();
    private static final Logger log = LoggerFactory.getLogger(GoogleSheetService.class);

    private final EmployeeRepositoryMongo employeeRepositoryMongo;
    private final GroupQuestRepositoryMongo groupQuestRepositoryMongo;
    private final LeaderQuestRepository leaderQuestRepository;
    private final BoardRepositoryMongo boardRepositoryMongo;
    private final LeaderQuestExpRepository leaderQuestExpRepository;
    private final ProjectRepository projectRepository;
    private final EvaluationRepository evaluationRepository;

    // TODO : Google Sheets API를 Service layer에서 분리해야 함
    public List<Object> getSheetData(String spreadsheetId, String range) throws IOException, GeneralSecurityException {
        try {
            // GoogleCredential 생성
            GoogleCredential credential = GoogleCredential
                    .fromStream(new FileInputStream(CREDENTIALS_FILE_PATH))
                    .createScoped(List.of("https://www.googleapis.com/auth/spreadsheets"));

            // Sheets API 클라이언트 생성
            Sheets sheetsService = new Sheets.Builder(
                    credential.getTransport(),
                    credential.getJsonFactory(),
                    credential
            )
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // API 호출
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            // 응답 값 로그
            log.info("Google Sheets API Response: {}", response.getValues());

            return response.getValues() != null ? response.getValues().get(0) : List.of(); // 첫 번째 행 반환
        } catch (IOException e) {
            log.error("Error accessing Google Sheets API: ", e); // 예외 발생 시 로그 남기기
            throw new RuntimeException("Error accessing Google Sheets API: " + e.getMessage());
        }
    }

    @Transactional
    public void syncGoogleSheetToMongo(String spreadsheetId, String range) throws Exception {
        try {
            // Google Sheets에서 데이터 읽기
            List<List<Object>> sheetData = googleSheetsHelper.readSheetData(spreadsheetId, range);

            // 데이터 읽기 확인
            log.info("Sheet Data Read: {}", sheetData);

            // 데이터를 User 객체로 변환
            List<Employee> employees = GoogleSheetsConvert.convertToUsers(sheetData);
            for(Employee employee : employees) {
                System.out.println(employee);
            }

            // MongoDB에 저장 및 저장된 결과 확인
            List<Employee> savedEmployees = employeeRepositoryMongo.saveAll(employees);

            // 저장 성공 여부 확인
            if (savedEmployees.size() == employees.size()) {
                log.info("All users saved successfully.");
            } else {
                log.warn("Some users were not saved.");
            }
        } catch (Exception e) {
            log.error("Error while syncing Google Sheets to MongoDB: ", e); // 예외 발생 시 로그 남기기
            throw new RuntimeException("Error while syncing Google Sheets to MongoDB: " + e.getMessage());
        }
    }

    @Transactional
    public void syncAll(String spreadSheetId, String employeeRange, String groupQuestRange,String leaderQuestRange) {
        try {
            syncGroupQuestData(spreadSheetId, groupQuestRange);
            syncLeaderQuestData(spreadSheetId, leaderQuestRange);

            log.info("GroupQuest 데이터 동기화 완료");
        } catch (Exception e) {
            log.error("Google Sheets 동기화 중 오류 발생: ", e);
            throw new RuntimeException("MongoDB 동기화 중 오류 발생: " + e.getMessage());
        }
    }

    private void syncGroupQuestData(String spreadSheetId, String groupQuestRange) throws Exception {

        List<GroupQuest> newGroupQuests = moduleGroupQuestData(spreadSheetId, groupQuestRange);

        for (GroupQuest newQuest : newGroupQuests) {
            // 먼저 기존의 모든 중복 데이터 삭제
            List<GroupQuest> duplicates = groupQuestRepositoryMongo.findAllByAffiliationAndDepartment(
                    newQuest.getAffiliation(),
                    newQuest.getDepartment()
            );

            if (!duplicates.isEmpty()) {
                // 가장 최근 데이터 하나만 남기고 나머지 삭제
                GroupQuest latestQuest = duplicates.get(0);
                duplicates.remove(0);
                if (!duplicates.isEmpty()) {
                    groupQuestRepositoryMongo.deleteAll(duplicates);
                }

                // 최근 데이터의 경험치 정보 업데이트
                latestQuest.setGroupExperiences(newQuest.getGroupExperiences());
                groupQuestRepositoryMongo.save(latestQuest);
                log.info("기존 GroupQuest 데이터 업데이트 완료: {}", latestQuest.getAffiliation());
            } else {
                // 새로운 데이터 저장
                groupQuestRepositoryMongo.save(newQuest);
                log.info("새로운 GroupQuest 데이터 저장 완료: {}", newQuest.getAffiliation());
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
                log.info("기존 LeaderQuest 데이터 삭제 완료: {}", affiliation);
            }

            // 새로운 데이터 저장
            leaderQuestRepository.saveAll(newLeaderQuests);
            log.info("새로운 LeaderQuest 데이터 저장 완료: {}", affiliation);

        } catch (Exception e) {
            log.error("Google Sheets 동기화 중 오류 발생: ", e);
            throw new RuntimeException("MongoDB 동기화 중 오류 발생: " + e.getMessage());
        }
    }

    private List<GroupQuest> moduleGroupQuestData(String spreadSheetId, String groupQuestRange) throws Exception {
        String groupRange = groupQuestRange + "!B2:D3";
        String expPerWeekRange = groupQuestRange + "!B5:D";
        String scoreInfo = groupQuestRange + "!F2:G3";

        List<List<Object>> groupData = googleSheetsHelper.readSheetData(spreadSheetId, groupRange);
        List<List<Object>> expPerWeekData = googleSheetsHelper.readSheetData(spreadSheetId, expPerWeekRange);
        List<List<Object>> scoreData = googleSheetsHelper.readSheetData(spreadSheetId, scoreInfo);

        List<GroupQuest> groupQuestList = GoogleSheetsConvert.convertToGroupQuest(groupData, expPerWeekData, scoreData);
        return groupQuestList;
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
            updatedPostData.add(List.of("번호", "제목", "글"));  // 헤더 추가

            for (Board board : boardsFromSheet) {
                if (board.getTitle() != null && !board.getTitle().isEmpty()) {
                    updatedPostData.add(List.of(
                            board.getId(),  // MongoDB의 _id를 사용
                            board.getTitle(),
                            board.getContent() != null ? board.getContent() : ""
                    ));
                }
            }

            // 시트 업데이트
            googleSheetsHelper.updateSheetData(spreadsheetId, boardRange, updatedPostData);
            log.info("MongoDB의 게시글 데이터를 Google Sheets에 업데이트 완료");

        } catch (Exception e) {
            log.error("게시글 동기화 중 오류 발생: ", e);
            throw new RuntimeException("게시글 동기화 중 오류 발생: " + e.getMessage());
        }
    }

    @Transactional
    public void syncLeaderQuestExp(String spreadSheetId, String leaderQuestRANGE) {
        try {
            String affiliationRange = leaderQuestRANGE + "!J8";
            String affiliation = googleSheetsHelper.readCell(spreadSheetId, affiliationRange);

            String leaderQuestRange = leaderQuestRANGE + "!B9:G";
            List<List<Object>> leaderQuestExpData = googleSheetsHelper.readSheetData(spreadSheetId, leaderQuestRange);
            List<LeaderQuestExp> leaderQuestExpList = GoogleSheetsConvert.convertToLeaderQuestExp(affiliation, leaderQuestExpData);

            // 기존 데이터 삭제
            List<LeaderQuestExp> existingLeaderQuestExps = leaderQuestExpRepository.findAllByAffiliation(affiliation);
            if (!existingLeaderQuestExps.isEmpty()) {
                leaderQuestExpRepository.deleteAll(existingLeaderQuestExps);
                log.info("기존 LeaderQuestExp 데이터 삭제 완료: {}", affiliation);
            }

            // 새로운 데이터 저장
            leaderQuestExpRepository.saveAll(leaderQuestExpList);
            log.info("새로운 LeaderQuestExp 데이터 저장 완료: {}", affiliation);
        } catch (Exception e) {
            log.error("Google Sheets 동기화 중 오류 발생: ", e);
            throw new RuntimeException("MongoDB 동기화 중 오류 발생: " + e.getMessage());
        }
    }

    @Transactional
    public void syncProject(String spreadSheetId, String projectRANGE) {
        try {
            List<List<Object>> projectData = googleSheetsHelper.readSheetData(spreadSheetId, projectRANGE);
            List<Project> projectList = GoogleSheetsConvert.convertToProject(projectData);

            // 기존 데이터 삭제
            projectRepository.deleteAll();
            log.info("기존 Project 데이터 삭제 완료.");

            // 새로운 데이터 저장
            projectRepository.saveAll(projectList);
            log.info("새로운 Project 데이터 저장 완료.");
        } catch (Exception e) {
            log.error("Google Sheets 동기화 중 오류 발생: ", e);
            throw new RuntimeException("MongoDB 동기화 중 오류 발생: " + e.getMessage());
        }
    }

    @Transactional
    public void syncEvaluation(String spreadSheetId, String evaluationRANGE) {
        try {
            // 상반기, 하반기 평가 데이터 읽기
            String firstHalfRange = evaluationRANGE + "!B7";
            String secondHalfRange = evaluationRANGE + "!H7";

            String firstHalf = googleSheetsHelper.readCell(spreadSheetId, firstHalfRange);
            String secondHalf = googleSheetsHelper.readCell(spreadSheetId, secondHalfRange);

            // 평가 데이터 읽기
            String firstHalfEvaluationDataRange = evaluationRANGE + "!B9:F";
            String secondHalfEvaluationDataRange = evaluationRANGE + "!H9:L";

            List<List<Object>> firstHalfEvaluationData = googleSheetsHelper.readSheetData(spreadSheetId, firstHalfEvaluationDataRange);
            List<List<Object>> secondHalfEvaluationData = googleSheetsHelper.readSheetData(spreadSheetId, secondHalfEvaluationDataRange);

            List<Evaluation> firstHalfEvaluationList = GoogleSheetsConvert.convertToEvaluation(firstHalf, firstHalfEvaluationData);
            List<Evaluation> secondHalfEvaluationList = GoogleSheetsConvert.convertToEvaluation(secondHalf, secondHalfEvaluationData);

            // 기존 데이터 삭제
            evaluationRepository.deleteAll();

            // 새로운 데이터 저장
            evaluationRepository.saveAll(firstHalfEvaluationList);
            evaluationRepository.saveAll(secondHalfEvaluationList);

        } catch (Exception e) {
            log.error("Google Sheets 동기화 중 오류 발생: ", e);
            throw new RuntimeException("MongoDB 동기화 중 오류 발생: " + e.getMessage());
        }

    }
}