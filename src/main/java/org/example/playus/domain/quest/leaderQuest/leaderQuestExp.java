package org.example.playus.domain.quest.leaderQuest;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "leaderQuestExp")
public class leaderQuestExp {
    private String affiliation; // 소속

    private LeaderQuestEmployeeList leaderQuestEmployeeList; // 리더 퀘스트 직원

    @Builder
    public leaderQuestExp(String affiliation, LeaderQuestEmployeeList leaderQuestEmployeeList) {
        this.affiliation = affiliation;
        this.leaderQuestEmployeeList = leaderQuestEmployeeList;
    }
}
