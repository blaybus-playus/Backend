package org.example.playus.domain.quest.groupGuset;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

@Getter
public class WeeklyInfo {
    @Id
    private String id;
    private int maxScore; // 최대 점수
    private int midiumScore; // 중간 점수
    private double maxRate; // 최대 비율
    private double midiumRate; // 중간 비율
    private int week; // 주차
    private double productivity; // 생산성

    private QuestDetail questDetail; // 퀘스트 상세

    @Builder
    public WeeklyInfo(int maxScore, int midiumScore, double maxRate, double midiumRate, int week, double productivity, QuestDetail questDetail) {
        this.maxScore = maxScore;
        this.midiumScore = midiumScore;
        this.maxRate = maxRate;
        this.midiumRate = midiumRate;
        this.week = week;
        this.productivity = productivity;
        this.questDetail = questDetail;
    }
}
