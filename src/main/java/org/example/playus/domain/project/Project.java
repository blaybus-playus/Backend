package org.example.playus.domain.project;

import lombok.Builder;
import lombok.Getter;
import org.example.playus.global.Timestamped;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "project")
public class Project extends Timestamped {
    private int month; // 월
    private int day; // 일
    private int employeeId; // 직원 ID
    private String employeeName; // 직원 이름
    private String projectTitle; // 프로젝트 제목
    private int score; // 점수

    @Builder
    public Project(int month, int day, int employeeId, String employeeName, String projectTitle, int score) {
        this.month = month;
        this.day = day;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.projectTitle = projectTitle;
        this.score = score;
    }
}
