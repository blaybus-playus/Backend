package org.example.playus.domain.level;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LevelRepository extends MongoRepository<Level, String> {
    Optional<Level> findByLevelGroup(String levelGroupInitial);
}
