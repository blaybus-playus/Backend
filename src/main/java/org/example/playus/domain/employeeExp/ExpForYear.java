package org.example.playus.domain.employeeExp;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ExpForYear {
    private int totalExp; // 2024년 획득한 총 경험치
    private int firstHalfEvaluationExp; // 상반기 인사평가
    private int secondHalfEvaluationExp; // 하반기 인사평가
    private int groupQuestExp; // 직무별 퀘스트
    private int leaderQuestExp; // 리더부여 퀘스트
    private int projectExp; // 전사 프로젝트

    @Builder
    public ExpForYear(int totalExp, int firstHalfEvaluationExp, int secondHalfEvaluationExp, int groupQuestExp, int leaderQuestExp, int projectExp) {
        this.totalExp = totalExp;
        this.firstHalfEvaluationExp = firstHalfEvaluationExp;
        this.secondHalfEvaluationExp = secondHalfEvaluationExp;
        this.groupQuestExp = groupQuestExp;
        this.leaderQuestExp = leaderQuestExp;
        this.projectExp = projectExp;
    }
}
