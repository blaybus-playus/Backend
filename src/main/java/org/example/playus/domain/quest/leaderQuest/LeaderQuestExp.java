package org.example.playus.domain.quest.leaderQuest;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "LeaderQuestExp")
public class LeaderQuestExp {
    private String affiliation; // 소속

    private LeaderQuestEmployeeList leaderQuestEmployeeList; // 리더 퀘스트 직원

    @Builder
    public LeaderQuestExp(String affiliation, LeaderQuestEmployeeList leaderQuestEmployeeList) {
        this.affiliation = affiliation;
        this.leaderQuestEmployeeList = leaderQuestEmployeeList;
    }
}
