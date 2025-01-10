package org.example.playus.domain.quest.groupGuset;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Document(collection = "groupQuest")
public class GroupQuest {
    @Id
    private String id; // 그룹 퀘스트 ID

    private String affiliation; // 소속
    private int department; // 부서
    private String period; // 기간
    private int maxScore; // 최대 점수
    private int mediumScore; // 중간 점수

    @Setter
    private List<GroupExperience> groupExperiences; // 그룹 경험치 리스트

    @Builder
    public GroupQuest(String affiliation, int department, String period, int maxScore, int mediumScore, List<GroupExperience> groupExperiences) {
        this.affiliation = affiliation;
        this.department = department;
        this.period = period;
        this.maxScore = maxScore;
        this.mediumScore = mediumScore;
        this.groupExperiences = groupExperiences != null ? groupExperiences : new ArrayList<>();
    }

    public void setGroupExperience(GroupExperience groupExperience) {
        this.groupExperiences.add(groupExperience);
    }
}
