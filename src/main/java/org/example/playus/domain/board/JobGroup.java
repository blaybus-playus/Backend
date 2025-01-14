package org.example.playus.domain.board;

import lombok.Getter;

@Getter
public enum JobGroup {
    TECH("기술직군"),
    FIELD("현장직군"),
    MANAGEMENT("관리직군"),
    GROWTH("성장전략"),
    ALL("전체직군")
    ;
    private final String description;

    JobGroup(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
