package org.example.playus.domain.employeeExp;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.playus.global.Timestamped;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "employeeExp")
public class EmployeeExp extends Timestamped {
    private String title; // 테이블 명
    private int employeeId; // 사번
    private String name; // 이름
    private String affiliation; // 소속
    private int department; // 부서
    private String level; // 레벨

    @Setter
    private int year; // 년도
    @Setter
    private int maxExp; // 최대 경험치

    private ExpForYear expForYear;

    @Builder
    public EmployeeExp(String title, int employeeId, String name, String affiliation, int department, String level, int year, int maxExp, ExpForYear expForYear) {
        this.title = title;
        this.employeeId = employeeId;
        this.name = name;
        this.affiliation = affiliation;
        this.department = department;
        this.level = level;
        this.year = year;
        this.maxExp = maxExp;
        this.expForYear = expForYear;
    }
}
