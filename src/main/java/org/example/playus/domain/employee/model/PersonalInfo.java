package org.example.playus.domain.employee.model;

import lombok.Getter;

@Getter
public class PersonalInfo {

    private String name;         // 이름
    private String joinDate;     // 입사일
    private String department;   // 소속
    private String jobGroup;     // 직무그룹
    private String level;        // 레벨

    // Getters and Setters

    public void setName(String name) {
        this.name = name;
    }

    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}

