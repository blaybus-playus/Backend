package org.example.playus.domain.quest.leaderQuest;

import lombok.RequiredArgsConstructor;
import org.example.playus.global.common.CommonResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LeaderQuestController {

    private final LeaderQuestService leaderQuestService;

    @Value("${google.spreadsheet.id}")
    private String spreadSheetId; // 스프레드시트 ID

    @PostMapping("/leaderQuest")
    public ResponseEntity<String> syncLeaderQuest() {
        try {
            leaderQuestService.syncLeaderQuest(spreadSheetId);
            return ResponseEntity.ok("리더 퀘스트 동기화 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리더 퀘스트 동기화 실패: " + e.getMessage());
        }
    }
}
