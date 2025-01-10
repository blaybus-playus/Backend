package org.example.playus.domain.quest.leaderQuest;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.playus.domain.sheet.GoogleSheetsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderQuestService {

    private static final Logger log = LoggerFactory.getLogger(LeaderQuestService.class);
    private LeaderQuestRepository leaderQuestRepository;
    private static final GoogleSheetsHelper googleSheetsHelper = new GoogleSheetsHelper();

    @Transactional
    public void syncLeaderQuest(String spreadSheetId) {
        try {
            String affiliation = googleSheetsHelper.readCell(spreadSheetId,"리더부여 퀘스트!J8");

            List<List<Object>> sheetData = googleSheetsHelper.readSheetData(spreadSheetId, "리더부여 퀘스트!J10:Q14");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("리더 퀘스트 동기화 실패: " + e.getMessage());
        }
    }
}
