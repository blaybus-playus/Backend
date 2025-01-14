package org.example.playus.domain.evaluation;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonalEvaluation {
    private int employeeId;  // 사번
    private String name;        // 대상자
    private String grade;       // 인사평가 등급
    private int experience;     // 부여 경험치
    private String note;        // 비고

    @Builder
    public PersonalEvaluation(int employeeId, String name, String grade, int experience, String note) {
        this.employeeId = employeeId;
        this.name = name;
        this.grade = grade;
        this.experience = experience;
        this.note = note;
    }
}
