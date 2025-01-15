package org.example.playus.domain.quest.groupGuset;

import lombok.Builder;
import lombok.Getter;

@Getter
public class GroupQeustInfo {
    private int weekOrMonth;
    private int score;
    private String etc;

    @Builder
    public GroupQeustInfo(int weekOrMonth, int score, String etc) {
        this.weekOrMonth = weekOrMonth;
        this.score = score;
        this.etc = etc;
    }
}
