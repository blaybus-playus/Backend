package org.example.playus.domain.employee.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.playus.domain.quest.groupGuset.QuestInfo;

import java.util.List;

@Getter
public class EmployeeGroupQuestResponseDto {
    private String employeeId;
    private String employeeName;
    private String affiliation;
    private int department;
    private String period;
    private int maxScore;
    private int mediumScore;
    private List<QuestInfo> questInfoList;

    @Builder
    public EmployeeGroupQuestResponseDto(String employeeId, String employeeName, String affiliation, int department, String period, int maxScore, int mediumScore, List<QuestInfo> questInfoList) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.affiliation = affiliation;
        this.department = department;
        this.period = period;
        this.maxScore = maxScore;
        this.mediumScore = mediumScore;
        this.questInfoList = questInfoList;
    }
}
