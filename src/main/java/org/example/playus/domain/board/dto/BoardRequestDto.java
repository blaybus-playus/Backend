package org.example.playus.domain.board.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class BoardRequestDto {

    private String title;
    private String content;

    @Builder
    public BoardRequestDto(String title,String content) {
        this.title = title;
        this.content = content;
    }
}
