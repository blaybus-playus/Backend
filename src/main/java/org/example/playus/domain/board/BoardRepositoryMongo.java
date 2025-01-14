package org.example.playus.domain.board;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepositoryMongo extends MongoRepository<Board, String> {
    List<Board> findAllByOrderByIdAsc();

    List<Board> findByJobGroup(JobGroup  jobGroup);

    // 제목 + 내용으로 검색
    List<Board> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String title, String content);
}
