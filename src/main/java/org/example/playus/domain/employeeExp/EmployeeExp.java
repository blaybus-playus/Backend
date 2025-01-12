package org.example.playus.domain.employeeExp;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "employeeExp")
public class EmployeeExp {
    private String title; // 테이블 명
    private int employeeId; // 사번
    private String name; // 이름
    private String affiliation; // 소속
    private int department; // 부서
    private String level; // 레벨

    private ExpForYear expForYear;

    @Builder
    public EmployeeExp(String title, int employeeId, String name, String affiliation, int department, String level, ExpForYear expForYear) {
        this.title = title;
        this.employeeId = employeeId;
        this.name = name;
        this.affiliation = affiliation;
        this.department = department;
        this.level = level;
        this.expForYear = expForYear;
    }
}
