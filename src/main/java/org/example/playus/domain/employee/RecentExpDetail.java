package org.example.playus.domain.employee;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecentExpDetail {
    private String date; // 날짜
    private String questGroup; // 퀘스트 그룹
    private String questName; // 퀘스트 이름
    private int score; // 점수

    @Builder
    public RecentExpDetail(String date, String questGroup, String questName, int score) {
        this.date = date;
        this.questGroup = questGroup;
        this.questName = questName;
        this.score = score;
    }
}
