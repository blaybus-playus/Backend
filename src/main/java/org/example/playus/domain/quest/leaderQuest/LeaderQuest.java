package org.example.playus.domain.quest.leaderQuest;

import lombok.Builder;
import lombok.Getter;
import org.example.playus.global.Timestamped;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "leaderQuest")
public class LeaderQuest extends Timestamped {
    @Id
    private String id; // 리더 퀘스트 ID
    private String affiliation; // 소속

    private LeaderQuestList leaderQuestList; // 리더 퀘스트 리스트

    @Builder
    public LeaderQuest(String id, String affiliation, LeaderQuestList leaderQuestList) {
        this.id = id;
        this.affiliation = affiliation;
        this.leaderQuestList = leaderQuestList;
    }

}
