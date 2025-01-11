package org.example.playus.domain.board;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.playus.domain.board.dto.BoardRequestDto;
import org.example.playus.domain.board.dto.BoardResponseDto;
import org.example.playus.domain.sheet.GoogleSheetsHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepositoryMongo boardRepository;

    @Value("${google.spreadsheet.id}")
    private String spreadsheetId;

    private final String range = "게시판!B6:D";  // 시트 범위

    private static final GoogleSheetsHelper googleSheetsHelper = new GoogleSheetsHelper();

    @Transactional
    public BoardResponseDto addBoard(BoardRequestDto boardRequestDto) {
        log.info("게시글 등록 시작: 제목 = {}", boardRequestDto.getTitle());

        // 제목 검증
        if (boardRequestDto.getTitle() == null || boardRequestDto.getTitle().isBlank()) {
            throw new IllegalArgumentException("게시글 제목은 비어 있을 수 없습니다.");
        }
        int maxId = getMaxBoardId();

        // DB에 저장할 Board 생성
        int newId = maxId + 1;
        Board board = new Board();
        board.setId(String.valueOf(newId));
        board.setTitle(boardRequestDto.getTitle().trim());
        board.setContent(boardRequestDto.getContent().trim());

        // MongoDB에 자동 `ObjectId` 사용
        Board savedBoard = boardRepository.save(board);

        // 시트에 저장할 데이터 생성
        List<Object> row = List.of(savedBoard.getId(), savedBoard.getTitle(), savedBoard.getContent());

        try {
            googleSheetsHelper.appendRow(spreadsheetId, range, row);  // 시트에 데이터 추가
        } catch (Exception e) {
            log.error("스프레드시트 저장 실패: {}", e.getMessage());
            throw new RuntimeException("구글 시트에 데이터 추가 실패: " + e.getMessage());
        }

        log.info("게시글 등록 완료: ID = {}", savedBoard.getId());

        // 응답 DTO 반환
        return BoardResponseDto.builder()
                .id(savedBoard.getId())
                .title(savedBoard.getTitle())
                .content(savedBoard.getContent())
                .message("게시글이 등록되었습니다.")
                .build();
    }
    private int getMaxBoardId() {
        List<Board> boards = boardRepository.findAllByOrderByIdAsc();

        return boards.stream()
                .map(Board::getId)
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);  // 게시글이 없으면 0부터 시작
    }
}
