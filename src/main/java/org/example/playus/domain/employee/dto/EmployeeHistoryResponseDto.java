package org.example.playus.domain.employee.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class EmployeeHistoryResponseDto {
    private String employeeId; // 사원 번호
    private String employeeName; // 이름

    private String date; // 날짜
    private String questGroup; // 퀘스트 그룹
    private String questName; // 퀘스트 이름
    private int score; // 점수

    @Builder
    public EmployeeHistoryResponseDto(String employeeId, String employeeName, String date, String questGroup, String questName, int score) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.date = date;
        this.questGroup = questGroup;
        this.questName = questName;
        this.score = score;
    }
}
