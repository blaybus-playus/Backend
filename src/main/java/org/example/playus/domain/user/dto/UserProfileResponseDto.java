package org.example.playus.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponseDto {

    private String employeeId;
    private String username;
    private String name;
    private String joinDate;
    private String department;
    private String jobGroup;
    private String level;
    private String characterId;

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

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

    public void setCharacterId(String characterId) {
        this.characterId = characterId;
    }


}