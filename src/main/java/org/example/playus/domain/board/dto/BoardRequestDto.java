package org.example.playus.domain.board.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.playus.domain.board.JobGroup;

@Getter
public class BoardRequestDto {

    private String title;
    private String content;
    private JobGroup jobGroup;

    @Builder
    public BoardRequestDto(String title, String content, JobGroup jobGroup) {
        this.title = title;
        this.content = content;
        this.jobGroup = jobGroup;  // 생성자에 추가
    }
}
