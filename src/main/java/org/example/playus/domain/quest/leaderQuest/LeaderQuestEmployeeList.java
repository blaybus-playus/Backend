package org.example.playus.domain.quest.leaderQuest;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
public class LeaderQuestEmployeeList {
    private int month; // 월
    private int employeeId; // 직원 ID
    private String employeeName; // 직원 이름
    @Setter
    private String questName; // 퀘스트 이름
    @Setter
    private String achievement; // 성과
    @Setter
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

    public boolean isDifferent(LeaderQuestEmployeeList other) {
        return !this.questName.equals(other.questName) || !this.achievement.equals(other.achievement) || this.score != other.score;
    }

}
