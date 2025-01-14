package org.example.playus.domain.board.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.playus.domain.board.JobGroup;

@Getter
@NoArgsConstructor
public class BoardResponseDto {

    private String id;       // 게시글 ID
    private String title;    // 게시글 제목
    private String content;  // 게시글 내용
    private JobGroup jobGroup;
    private boolean success; // 요청 성공 여부
    private String message;  // 응답 메시지

    @Builder
    public BoardResponseDto(String id, String title, String content,JobGroup jobGroup ,boolean success, String message) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.jobGroup = jobGroup;
        this.success = success;
        this.message = message;
    }

    // 성공 응답을 생성하는 정적 메서드
    public static BoardResponseDto success(String id, String title, String content, String message) {
        return BoardResponseDto.builder()
                .id(id)
                .title(title)
                .content(content)
                .success(true)
                .message(message)
                .build();
    }

    // 실패 응답을 생성하는 정적 메서드
    public static BoardResponseDto failure(String message) {
        return BoardResponseDto.builder()
                .success(false)
                .message(message)
                .build();
    }
}