package org.example.playus.domain.board;

import lombok.Getter;

@Getter
public enum JobGroup {
    T("기술직군"),
    F("현장직군"),
    B("관리직군"),
    G("성장전략"),
    A("전체직군")
    ;
    private final String description;

    JobGroup(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
