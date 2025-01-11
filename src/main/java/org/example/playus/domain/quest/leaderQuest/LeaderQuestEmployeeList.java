package org.example.playus.domain.quest.leaderQuest;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LeaderQuestEmployeeList {
    private int month; // 월
    private int employeeId; // 직원 ID
    private String employeeName; // 직원 이름
    private String questName; // 퀘스트 이름
    private String achievement; // 성과
    private int score; // 점수

    @Builder
    public LeaderQuestEmployeeList(int month, int employeeId, String employeeName, String questName, String achievement, int score) {
        this.month = month;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.questName = questName;
        this.achievement = achievement;
        this.score = score;
    }
}
