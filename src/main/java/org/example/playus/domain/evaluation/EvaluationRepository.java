package org.example.playus.domain.evaluation;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvaluationRepository extends MongoRepository<Evaluation, String> {
    Optional<Evaluation> findAllByTerm(String term);
}
