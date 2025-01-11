package org.example.playus.domain.board;

import lombok.RequiredArgsConstructor;
import org.example.playus.domain.board.dto.BoardRequestDto;
import org.example.playus.domain.board.dto.BoardResponseDto;
import org.example.playus.global.common.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping("/add")
    public ResponseEntity<CommonResponse<BoardResponseDto>> addBoard(@RequestBody BoardRequestDto boardRequestDto) {
        try {
            // 서비스 호출 및 게시글 추가 처리
            BoardResponseDto responseDto = boardService.addBoard(boardRequestDto);

            // 성공 응답 반환
            CommonResponse<BoardResponseDto> response = new CommonResponse<>("게시글이 성공적으로 등록되었습니다.", 200, responseDto);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Bad Request 응답 처리
            CommonResponse<BoardResponseDto> errorResponse = new CommonResponse<>("잘못된 요청: " + e.getMessage(), 400, null);
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (RuntimeException e) {
            // Internal Server Error 응답 처리
            CommonResponse<BoardResponseDto> errorResponse = new CommonResponse<>("서버 오류: " + e.getMessage(), 500, null);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
