package org.example.playus.domain.level;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LevelExp {
    private String level;
    private int exp;

    @Builder
    public LevelExp(String level, int exp) {
        this.level = level;
        this.exp = exp;
    }
}
