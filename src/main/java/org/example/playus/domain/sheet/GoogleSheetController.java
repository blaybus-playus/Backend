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
@Tag(name ="GoogleSheet", description = "SheetController APIs")
public class GoogleSheetController {

    private final GoogleSheetService googleSheetService;

    @Value("${google.spreadsheet.id}")
    private String spreadSheetId; // 스프레드시트 ID

    private static final String RANGE = "요구사항!B2:D2"; // 읽을 셀 범위
    private static final String EmployeeRANGE = "시트10!B2:V"; // 읽을 셀 범위
    private static final String GroupQuestRANGE = "직무퀘 음성 1센터 1그룹"; // 읽을 셀 범위

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


    @PostMapping("/sync")
    @Operation(summary = "sheet sync", description = "데이터 동기화기능")
    public ResponseEntity<String> syncUsers() {
        try {
            googleSheetService.syncGoogleSheetToMongo(spreadSheetId, EmployeeRANGE);
            return ResponseEntity.ok("데이터 동기화 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("동기화 실패: " + e.getMessage());
        }
    }

    @PostMapping("sync/group/quest")
    public ResponseEntity<String> syncGroupQuest() {
        try {
            googleSheetService.syncGroupQuest(spreadSheetId, GroupQuestRANGE);
            return ResponseEntity.ok("그룹 퀘스트 동기화 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("그룹 퀘스트 동기화 실패: " + e.getMessage());
        }
    }
    @PutMapping("/sync")
    public ResponseEntity<String> syncSheetAndMongo() {
        try {
            googleSheetService.syncAll(spreadSheetId, EmployeeRANGE, GroupQuestRANGE);
            return ResponseEntity.ok("데이터 동기화 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("동기화 실패: " + e.getMessage());
        }
    }
}
