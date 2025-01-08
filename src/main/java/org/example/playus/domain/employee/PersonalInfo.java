package org.example.playus.domain.employee;

import lombok.Getter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
public class PersonalInfo {
    @Field
    @Indexed(unique = true)
    private int conum;

    private String name;         // 이름
    private String joinDate;     // 입사일
    private String department;   // 소속
    private String jobGroup;     // 직무그룹
    private String level;        // 레벨

    // Getters and Setters
    public void setconum(int conum) {this.conum = conum;}

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

