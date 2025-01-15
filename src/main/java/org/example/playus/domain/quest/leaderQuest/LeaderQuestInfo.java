package org.example.playus.domain.quest.leaderQuest;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LeaderQuestInfo {
    private String questName;
    private String period;
    private int weekOrMonth;
    private String achievement;
    private int score;
    private String requireForMax;
    private String requireForMedium;

    @Builder
    public LeaderQuestInfo(String questName, String period, int weekOrMonth, String achievement, int score, String requireForMax, String requireForMedium) {
        this.questName = questName;
        this.period = period;
        this.weekOrMonth = weekOrMonth;
        this.achievement = achievement;
        this.score = score;
        this.requireForMax = requireForMax;
        this.requireForMedium = requireForMedium;
    }
}
