package org.example.playus.domain.employee;

import lombok.Getter;
import org.example.playus.domain.admin.Admin;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Getter
@Document(collection = "employee")
public class Employee {

    @Id
    private String employeeId; // MongoDB에서 _id 역할
    private String characterId; // 캐릭터 ID

    private PersonalInfo personalInfo; // 하위 문서
    private Account account;         // 하위 문서
    private Map<String, Integer> points; // 연도별 포인트 데이터를 Map으로 저장
    private TokenStore tokenStore;

    private Admin admin;

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public void setCharacterId(String characterId) {
        this.characterId = characterId;
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

    public void setTokenStore(TokenStore tokenStore) {this.tokenStore = tokenStore;}

    public void setAdmin(Admin admin) {this.admin = admin;}

}

