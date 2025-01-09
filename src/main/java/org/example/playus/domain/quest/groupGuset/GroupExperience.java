package org.example.playus.domain.quest.groupGuset;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
public class GroupExperience {
    private int week; // 주차
    @Setter
    private int experience; // 경험치
    @Setter
    private String etc; // 기타

    @Builder
    public GroupExperience(int week, int experience, String etc) {
        this.week = week;
        this.experience = experience;
        this.etc = etc;
    }
}