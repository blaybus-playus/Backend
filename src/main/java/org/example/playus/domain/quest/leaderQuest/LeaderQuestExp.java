package org.example.playus.domain.quest.leaderQuest;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.playus.global.Timestamped;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "LeaderQuestExp")
public class LeaderQuestExp extends Timestamped {
    @Id
    private String id; // ID

    private String affiliation; // 소속

    @Setter
    private LeaderQuestEmployeeList leaderQuestEmployeeList; // 리더 퀘스트 직원

    @Builder
    public LeaderQuestExp(String id, String affiliation, LeaderQuestEmployeeList leaderQuestEmployeeList) {
        this.id = id;
        this.affiliation = affiliation;
        this.leaderQuestEmployeeList = leaderQuestEmployeeList;
    }
}
