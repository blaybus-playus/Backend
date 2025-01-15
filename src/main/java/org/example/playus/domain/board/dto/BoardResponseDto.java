package org.example.playus.domain.board.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.playus.domain.board.JobGroup;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class BoardResponseDto {

    private String id;       // 게시글 ID
    private String title;    // 게시글 제목
    private String content;  // 게시글 내용
    private JobGroup jobGroup;
    private LocalDateTime modifiedAt;
    private boolean success; // 요청 성공 여부
    private String message;  // 응답 메시지

    @Builder
    public BoardResponseDto(String id, String title, String content,LocalDateTime modifiedAt, JobGroup jobGroup ,boolean success, String message) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.jobGroup = jobGroup;
        this.modifiedAt = modifiedAt;
        this.success = success;
        this.message = message;
    }
}