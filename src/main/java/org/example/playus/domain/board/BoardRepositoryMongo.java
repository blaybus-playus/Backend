package org.example.playus.domain.board;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepositoryMongo extends MongoRepository<Board, String> {
    List<Board> findAllByOrderByIdAsc();
}
