package org.example.playus.domain.board;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.playus.domain.board.dto.BoardRequestDto;
import org.example.playus.domain.board.dto.BoardResponseDto;
import org.example.playus.domain.sheet.GoogleSheetsHelper;
import org.example.playus.global.exception.CustomException;
import org.example.playus.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.undo.CannotUndoException;
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
                throw new CustomException(ErrorCode.INVALID_TYPE);
        }

        if (boards.isEmpty()) {
            throw new CustomException(ErrorCode.SEARCH_NOT_FOUND);
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
                .orElseThrow(() -> new CustomException(ErrorCode.SHEET_NOT_FOUND));

        board.setTitle(boardRequestDto.getTitle().trim());
        board.setContent(boardRequestDto.getContent().trim());
        Board updatedBoard = boardRepository.save(board);

        try {
            List<List<Object>> sheetData = googleSheetsHelper.readSheetData(spreadsheetId, "게시판!A:Z");
            int rowIndex = -1;

            for (int i = 1; i < sheetData.size(); i++) {  // 첫 행은 헤더
                List<Object> row = sheetData.get(i);
                log.info("Google Sheets 행 번호 {} 데이터: {}", i + 1, row);  // 로그 추가
                if (row.size() >= 2 && row.get(1).toString().equals(String.valueOf(id))) {  // B열에서 ID 비교
                    rowIndex = i + 1;  // 실제 행 번호
                    break;
                }
            }

            if (rowIndex == -1) {
                log.error("Google Sheets에서 해당 ID의 게시글을 찾을 수 없습니다.");
                throw new CustomException(ErrorCode.SHEET_NOT_FOUND);
            }

            // Google Sheets 업데이트
            List<Object> updatedRow = List.of(updatedBoard.getId(), updatedBoard.getTitle(), updatedBoard.getContent());
            String rowRange = String.format("게시판!B%d:D%d", rowIndex, rowIndex);
            googleSheetsHelper.updateRow(spreadsheetId, rowRange, updatedRow);
            log.info("Google Sheets 업데이트 완료: 행 번호 = {}", rowIndex);
        } catch (Exception e) {
            log.error("스프레드시트 업데이트 실패: {}", e.getMessage());
            throw new RuntimeException("구글 시트에 데이터 업데이트 실패: " + e.getMessage());
        }

        return BoardResponseDto.builder()
                .id(updatedBoard.getId())
                .title(updatedBoard.getTitle())
                .content(updatedBoard.getContent())
                .message("게시글이 성공적으로 수정되었습니다.")
                .build();
    }

    public void deleteBoard(Long id) {
        log.info("게시글 삭제 시작: ID = {}", id);

        Board board = boardRepository.findById(String.valueOf(id))
                .orElseThrow(() -> new CustomException(ErrorCode.SHEET_NOT_FOUND));

        boardRepository.delete(board);
        log.info("MongoDB에서 게시글 삭제 완료: ID = {}", id);

        try {
            List<List<Object>> sheetData = googleSheetsHelper.readSheetData(spreadsheetId, "게시판!A:Z");
            int rowIndex = -1;

            for (int i = 1; i < sheetData.size(); i++) {
                List<Object> row = sheetData.get(i);
                if (row.size() >= 2 && row.get(1).toString().equals(String.valueOf(id))) {
                    rowIndex = i + 1;  // 실제 행 번호
                    break;
                }
            }

            if (rowIndex == -1) {
                log.error("Google Sheets에서 해당 ID의 게시글을 찾을 수 없습니다.");
                throw new IllegalArgumentException("Google Sheets에서 해당 ID의 게시글을 찾을 수 없습니다.");
            }

            // 행 삭제
            googleSheetsHelper.deleteRow(spreadsheetId, "게시판", rowIndex);
            log.info("Google Sheets에서 행 삭제 완료: 행 번호 = {}", rowIndex);
        } catch (Exception e) {
            log.error("Google Sheets 행 삭제 실패: {}", e.getMessage());
            throw new RuntimeException("Google Sheets 행 삭제 실패: " + e.getMessage());
        }
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
