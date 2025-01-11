package org.example.playus.domain.sheet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/google")
@RequiredArgsConstructor
@Tag(name = "GoogleSheet", description = "SheetController APIs")
public class GoogleSheetController {

    private final GoogleSheetService googleSheetService;

    @Value("${google.spreadsheet.id}")
    private String spreadSheetId; // 스프레드시트 ID

    private static final String RANGE = "요구사항!B2:D2"; // 읽을 셀 범위
    private static final String EmployeeRANGE = "시트10!B2:V"; // 직원 데이터 범위
    private static final String GroupQuestRANGE = "직무퀘 음성 1센터 1그룹"; // 그룹 퀘스트 범위
    private static final String LeaderQuestRANGE = "리더부여 퀘스트"; // 리더 퀘스트 범위
    private static final String BoardRANGE = "게시판!B6:D"; // 게시판 범위

    // 데이터 조회
    @GetMapping("/read")
    @Operation(summary = "sheet read", description = "데이터 조회하는 기능")
    public ResponseEntity<List<Object>> readFromSheet() {
        try {
            List<Object> data = googleSheetService.getSheetData(spreadSheetId, RANGE);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }

    // 전체 데이터 동기화
    @PutMapping("/sync/all")
    @Operation(summary = "전체 데이터 동기화", description = "모든 데이터를 동기화")
    public ResponseEntity<String> syncAllData() {
        try {
            googleSheetService.syncAll(spreadSheetId, EmployeeRANGE, GroupQuestRANGE, LeaderQuestRANGE);
            return ResponseEntity.ok("전체 데이터 동기화 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("전체 동기화 실패: " + e.getMessage());
        }
    }

    // 직원 데이터 동기화
    @PostMapping("/sync/employees")
    @Operation(summary = "직원 데이터 동기화", description = "직원 데이터를 MongoDB와 동기화")
    public ResponseEntity<String> syncEmployees() {
        try {
            googleSheetService.syncGoogleSheetToMongo(spreadSheetId, EmployeeRANGE);
            return ResponseEntity.ok("직원 데이터 동기화 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("직원 데이터 동기화 실패: " + e.getMessage());
        }
    }

    // 리더 퀘스트 데이터 동기화
    @PostMapping("/sync/leader/quest")
    @Operation(summary = "리더 퀘스트 동기화", description = "리더 퀘스트 데이터를 동기화")
    public ResponseEntity<String> syncLeaderQuest() {
        try {
            googleSheetService.syncLeaderQuestData(spreadSheetId, LeaderQuestRANGE);
            return ResponseEntity.ok("리더 퀘스트 동기화 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리더 퀘스트 동기화 실패: " + e.getMessage());
        }
    }

    // 게시판 데이터 동기화
    @PostMapping("/sync/board")
    @Operation(summary = "게시판 데이터 동기화", description = "게시판 데이터를 동기화")
    public ResponseEntity<String> syncBoard() {
        try {
            googleSheetService.syncBoard(spreadSheetId, BoardRANGE);
            return ResponseEntity.ok("게시판 동기화 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시판 동기화 실패: " + e.getMessage());
        }
    }

    // 리더퀘스트 경험치 부분 데이터 동기화
    @PostMapping("/sync/leader/quest/exp")
    @Operation(summary = "리더 퀘스트 경험치 부분 동기화", description = "리더 퀘스트 경험치 부분 데이터를 동기화")
    public ResponseEntity<String> syncLeaderQuestExp() {
        try {
            googleSheetService.syncLeaderQuestExp(spreadSheetId, LeaderQuestRANGE);
            return ResponseEntity.ok("리더 퀘스트 경험치 부분 동기화 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리더 퀘스트 경험치 부분 동기화 실패: " + e.getMessage());
        }
    }
}