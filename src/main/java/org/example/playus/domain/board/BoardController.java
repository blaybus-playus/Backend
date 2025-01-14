package org.example.playus.domain.board;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.playus.domain.board.dto.BoardRequestDto;
import org.example.playus.domain.board.dto.BoardResponseDto;
import org.example.playus.domain.security.service.UserDetailsImpl;
import org.example.playus.global.common.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping("/add")
    @Operation(summary = "게시판 생성", description = "게시글을 생성 admin 계정일때만 작동합니다.")
    public ResponseEntity<CommonResponse<BoardResponseDto>> addBoard(
            @RequestBody BoardRequestDto boardRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (!"ROLE_ADMIN".equals(userDetails.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonResponse<>("접근 권한이 없습니다.", HttpStatus.FORBIDDEN.value(), null));
        }
        BoardResponseDto responseDto = boardService.addBoard(boardRequestDto);
        return ResponseEntity.ok(new CommonResponse<>("게시글이 성공적으로 등록되었습니다.", HttpStatus.OK.value(), responseDto));
    }

    @GetMapping("/read/all")
    @Operation(summary = "모든 게시글 조회", description = "모든 게시글 조회")
    public ResponseEntity<CommonResponse<List<BoardResponseDto>>> readAllBoards() {
        List<BoardResponseDto> boardList = boardService.readAllBoards();
        return ResponseEntity.ok(new CommonResponse<>("게시글 조회 성공", HttpStatus.OK.value(), boardList));
    }

    @GetMapping("/read/searchgroup")
    @Operation(summary = "직군별 게시글 조회", description = "직군 코드에 따라 게시글을 조회하거나 전체 게시글을 조회")
    public ResponseEntity<CommonResponse<List<BoardResponseDto>>> searchBoardByJobGroup(
            @RequestParam(required = false) JobGroup jobGroup
    ) {
        List<BoardResponseDto> responseDtos;

        if (jobGroup == null || jobGroup == JobGroup.ALL) {  // null 또는 ALL일 경우 전체 조회
            responseDtos = boardService.readAllBoards();
        } else {
            responseDtos = boardService.searchBoardByJobGroup(jobGroup);  // 특정 직군 조회
        }

        return ResponseEntity.ok(new CommonResponse<>("게시글 조회 성공", HttpStatus.OK.value(), responseDtos));
    }

    @GetMapping("/read/search")
    @Operation(summary = "키워드 기반 게시글 조회", description = "키워드로 제목, 내용, 직군 이름에서 검색")
    public ResponseEntity<CommonResponse<List<BoardResponseDto>>> searchBoard(
            @RequestParam String keyword
    ) {
        List<BoardResponseDto> responseDtos = boardService.searchBoardByKeyword(keyword);
        return ResponseEntity.ok(new CommonResponse<>("게시글 조회 성공", HttpStatus.OK.value(), responseDtos));
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "게시글 수정", description = "게시글 수정")
    public ResponseEntity<CommonResponse<BoardResponseDto>> updateBoard(
            @PathVariable String id,
            @RequestBody BoardRequestDto boardRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (!"ROLE_ADMIN".equals(userDetails.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonResponse<>("접근 권한이 없습니다.", HttpStatus.FORBIDDEN.value(), null));
        }
        BoardResponseDto responseDto = boardService.updateBoard(Long.valueOf(id), boardRequestDto);
        return ResponseEntity.ok(new CommonResponse<>("게시글이 성공적으로 수정되었습니다.", HttpStatus.OK.value(), responseDto));
    }

    @DeleteMapping("/delete/{id}")

    @Operation(summary = "게시글 삭제", description = "게시글 삭제 admin 계정일때만 작동합니다.")
    public ResponseEntity<CommonResponse<Void>> deleteBoard(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (!"ROLE_ADMIN".equals(userDetails.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonResponse<>("접근 권한이 없습니다.", HttpStatus.FORBIDDEN.value(), null));
        }
        boardService.deleteBoard(Long.valueOf(id));
        return ResponseEntity.ok(new CommonResponse<>("게시글이 성공적으로 삭제되었습니다.", HttpStatus.OK.value(), null));
    }
}