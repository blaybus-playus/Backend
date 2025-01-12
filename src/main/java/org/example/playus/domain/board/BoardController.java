package org.example.playus.domain.board;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.playus.domain.board.dto.BoardRequestDto;
import org.example.playus.domain.board.dto.BoardResponseDto;
import org.example.playus.global.common.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/readall")
    @Operation(summary = "read all", description = "모든 게시글 조회")
    public ResponseEntity<CommonResponse<List<BoardResponseDto>>> readAllBoards() {
        try {
            List<BoardResponseDto> boardList = boardService.readAllBoards();
            CommonResponse<List<BoardResponseDto>> response = new CommonResponse<>("게시글 조회 성공", 200, boardList);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            CommonResponse<List<BoardResponseDto>> errorResponse = new CommonResponse<>("서버 오류: " + e.getMessage(), 500, null);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/search")
    @Operation(summary = "search or read all", description = "제목, 내용, 제목+내용 검색 또는 전체 게시글 조회")
    public ResponseEntity<CommonResponse<List<BoardResponseDto>>> searchBoard(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String searchType
    ) {
        try {
            List<BoardResponseDto> responseDtos;

            // 검색어가 없으면 전체 조회
            if (keyword == null || keyword.isBlank()) {
                responseDtos = boardService.readAllBoards();  // 전체 게시글 조회 메서드 호출
            } else {
                responseDtos = boardService.searchBoard(keyword, searchType);
            }

            CommonResponse<List<BoardResponseDto>> response = new CommonResponse<>("게시글 조회 성공", 200, responseDtos);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            CommonResponse<List<BoardResponseDto>> errorResponse = new CommonResponse<>("잘못된 요청: " + e.getMessage(), 400, null);
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (RuntimeException e) {
            CommonResponse<List<BoardResponseDto>> errorResponse = new CommonResponse<>("서버 오류: " + e.getMessage(), 500, null);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "update", description = "게시글 수정")
    public ResponseEntity<CommonResponse<BoardResponseDto>> updateBoard(@PathVariable Long id, @RequestBody BoardRequestDto boardRequestDto) {
        try {
            BoardResponseDto responseDto = boardService.updateBoard(id, boardRequestDto);
            CommonResponse<BoardResponseDto> response = new CommonResponse<>("게시글이 성공적으로 수정되었습니다.", 200, responseDto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            CommonResponse<BoardResponseDto> errorResponse = new CommonResponse<>("잘못된 요청: " + e.getMessage(), 400, null);
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (RuntimeException e) {
            CommonResponse<BoardResponseDto> errorResponse = new CommonResponse<>("서버 오류: " + e.getMessage(), 500, null);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "delete", description = "게시글 삭제")
    public ResponseEntity<CommonResponse<Void>> deleteBoard(@PathVariable Long id) {
        try {
            boardService.deleteBoard(id);
            CommonResponse<Void> response = new CommonResponse<>("게시글이 성공적으로 삭제되었습니다.", 200, null);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            CommonResponse<Void> errorResponse = new CommonResponse<>("잘못된 요청: " + e.getMessage(), 400, null);
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (RuntimeException e) {
            CommonResponse<Void> errorResponse = new CommonResponse<>("서버 오류: " + e.getMessage(), 500, null);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}

