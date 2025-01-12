package org.example.playus.domain.board;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepositoryMongo extends MongoRepository<Board, String> {
    List<Board> findAllByOrderByIdAsc();

    // 제목으로 검색
    List<Board> findByTitleContainingIgnoreCase(String title);

    // 내용으로 검색
    List<Board> findByContentContainingIgnoreCase(String content);

    // 제목 + 내용으로 검색
    List<Board> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String title, String content);
}
