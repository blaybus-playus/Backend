package org.example.playus.domain.user.dto;

import lombok.Getter;

@Getter
public class UserUpdateRequestDtoForAdmin {


    private String department;
    private String jobGroup;
    private String level;


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
