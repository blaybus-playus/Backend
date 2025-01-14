package org.example.playus.domain.employee.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class EmployeeExpDetailResponseDto {
    private String name;
    private String employeeId;
    private int fistHalfExp;
    private int secondHalfExp;
    private int groupQuestExp;
    private int leaderQuestExp;
    private int projectExp;

    @Builder
    public EmployeeExpDetailResponseDto(String name, String employeeId, int fistHalfExp, int secondHalfExp, int groupQuestExp, int leaderQuestExp, int projectExp) {
        this.name = name;
        this.employeeId = employeeId;
        this.fistHalfExp = fistHalfExp;
        this.secondHalfExp = secondHalfExp;
        this.groupQuestExp = groupQuestExp;
        this.leaderQuestExp = leaderQuestExp;
        this.projectExp = projectExp;
    }
}
