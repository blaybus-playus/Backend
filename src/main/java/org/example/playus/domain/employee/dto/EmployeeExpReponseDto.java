package org.example.playus.domain.employee.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class EmployeeExpReponseDto {
    private String name; // 이름
    private int employeeId; // 사원 번호
    private String affiliation; // 소속
    private String characterId; // 캐릭터 ID
    private String level; // 레벨
    private int thisYearExp; // 올해 경험치
    private int totalExp; // 총 경험치
    private int nextLevelExp; // 다음 레벨까지 경험치
    private int limitExp; // 최대 경험치

    @Builder
    public EmployeeExpReponseDto(String name, int employeeId, String affiliation, String characterId,
                                 String level, int thisYearExp, int totalExp, int nextLevelExp, int limitExp) {
        this.name = name;
        this.employeeId = employeeId;
        this.affiliation = affiliation;
        this.characterId = characterId;
        this.level = level;
        this.thisYearExp = thisYearExp;
        this.totalExp = totalExp;
        this.nextLevelExp = nextLevelExp;
        this.limitExp = limitExp;
    }
}
