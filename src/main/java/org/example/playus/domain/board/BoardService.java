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

        // 새 ID 생성
        int newId = getMaxBoardId() + 1;

        // Board 객체 생성 후 값 설정
        Board board = new Board();
        board.setId(String.valueOf(newId));  // 새로운 ID 설정
        board.setTitle(boardRequestDto.getTitle().trim());  // 제목 설정
        board.setContent(boardRequestDto.getContent().trim());  // 내용 설정

        // MongoDB에 저장
        Board savedBoard = boardRepository.save(board);

        // 스프레드시트에 추가할 데이터 생성
        List<Object> row = List.of(savedBoard.getId(), savedBoard.getTitle(), savedBoard.getContent());
        try {
            googleSheetsHelper.appendRow(spreadsheetId, range, row);
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

    @Transactional(readOnly = true)
    public List<BoardResponseDto> readAllBoards() {
        log.info("모든 게시글 조회 시작");
        List<Board> boards = boardRepository.findAll();
        return boards.stream()
                .map(board -> BoardResponseDto.builder()
                        .id(board.getId())
                        .title(board.getTitle())
                        .content(board.getContent())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BoardResponseDto> searchBoard(String keyword, String searchType) {
        List<Board> boards;
        switch (searchType.toLowerCase()) {
            case "title":
                boards = boardRepository.findByTitleContainingIgnoreCase(keyword);
                break;
            case "content":
                boards = boardRepository.findByContentContainingIgnoreCase(keyword);
                break;
            case "title+content":
                boards = boardRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword);
                break;
            default:
                throw new IllegalArgumentException("잘못된 검색 타입입니다.");
        }

        if (boards.isEmpty()) {
            throw new IllegalArgumentException("검색 결과가 없습니다.");
        }

        return boards.stream()
                .map(board -> BoardResponseDto.builder()
                        .id(board.getId())
                        .title(board.getTitle())
                        .content(board.getContent())
                        .message("게시글 조회 성공")
                        .build())
                .toList();
    }

    @Transactional
    public BoardResponseDto updateBoard(Long id, BoardRequestDto boardRequestDto) {
        log.info("게시글 수정 시작: ID = {}", id);
        Board board = boardRepository.findById(String.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 게시글을 찾을 수 없습니다."));

        // 게시글 내용 업데이트
        board.setTitle(boardRequestDto.getTitle().trim());
        board.setContent(boardRequestDto.getContent().trim());

        Board updatedBoard = boardRepository.save(board);

        // Google Sheets 데이터 수정
        List<Object> updatedRow = List.of(updatedBoard.getId(), updatedBoard.getTitle(), updatedBoard.getContent());
        try {
            String rowRange = String.format("게시판!B%d:D%d", id + 6, id + 6);  // 행 번호 계산
            googleSheetsHelper.updateRow(spreadsheetId, rowRange, updatedRow);
        } catch (Exception e) {
            log.error("스프레드시트 업데이트 실패: {}", e.getMessage());
            throw new RuntimeException("구글 시트에 데이터 업데이트 실패: " + e.getMessage());
        }

        log.info("게시글 수정 완료: ID = {}", id);

        return BoardResponseDto.builder()
                .id(updatedBoard.getId())
                .title(updatedBoard.getTitle())
                .content(updatedBoard.getContent())
                .message("게시글이 성공적으로 수정되었습니다.")
                .build();
    }

    @Transactional
    public void deleteBoard(Long id) {
        log.info("게시글 삭제 시작: ID = {}", id);
        Board board = boardRepository.findById(String.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 게시글을 찾을 수 없습니다."));

        boardRepository.delete(board);

        // Google Sheets 데이터 비우기
        try {
            int rowIndex = id.intValue() + 6;  // Google Sheets의 행 번호 계산
            String rowRange = String.format("게시판!B%d:D%d", rowIndex, rowIndex);  // 특정 행 범위
            googleSheetsHelper.deleteRow(spreadsheetId, rowRange);  // 행을 빈 값으로 업데이트
        } catch (Exception e) {
            log.error("스프레드시트 비우기 실패: {}", e.getMessage());
            throw new RuntimeException("구글 시트에서 데이터 비우기 실패: " + e.getMessage());
        }

        log.info("게시글 삭제 완료: ID = {}", id);
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
