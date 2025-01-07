package org.example.playus.sheet;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Getter
@Document(collection = "users")
public class User {

    @Id
    private String employeeId; // MongoDB에서 _id 역할

    private PersonalInfo personalInfo; // 하위 문서
    private Account account;         // 하위 문서
    private Map<String, Integer> points; // 연도별 포인트 데이터를 Map으로 저장

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public void setPersonalInfo(PersonalInfo personalInfo) {
        this.personalInfo = personalInfo;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setPoints(Map<String, Integer> points) {
        this.points = points;
    }
}

