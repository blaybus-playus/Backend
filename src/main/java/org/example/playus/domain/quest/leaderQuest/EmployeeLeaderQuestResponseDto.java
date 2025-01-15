package org.example.playus.domain.quest.leaderQuest;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class EmployeeLeaderQuestResponseDto {
    private String employeeId;
    private String employeeName;
    private String affiliation;


    private List<LeaderQuestInfo> leaderQuestInfoList;

    @Builder
    public EmployeeLeaderQuestResponseDto(String employeeId, String employeeName, String affiliation, String requireForMax, String requireForMedium, List<LeaderQuestInfo> leaderQuestInfoList) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.affiliation = affiliation;
        this.leaderQuestInfoList = leaderQuestInfoList;
    }

}
