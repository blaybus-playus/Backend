package org.example.playus.domain.quest.leaderQuest;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LeaderQuestList {
    private String id; // 리더 퀘스트 리스트 ID
    private String questName; // 퀘스트 이름
    private String period; // 획득 주기
    private int totalScore; // 총 점수
    private int maxScore; // 최대 점수
    private int mediumScore; // 중간 점수
    private String requireForMax; // 최대 점수 요구 사항
    private String requireForMedium; // 중간 점수 요구 사항

    @Builder
    public LeaderQuestList(String id, String questName, String period, int totalScore, int maxScore, int mediumScore, String requireForMax, String requireForMedium) {
        this.id = id;
        this.questName = questName;
        this.period = period;
        this.totalScore = totalScore;
        this.maxScore = maxScore;
        this.mediumScore = mediumScore;
        this.requireForMax = requireForMax;
        this.requireForMedium = requireForMedium;
    }
}
