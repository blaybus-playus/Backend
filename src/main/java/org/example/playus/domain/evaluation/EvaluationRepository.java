package org.example.playus.domain.evaluation;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends MongoRepository<Evaluation, String> {
    List<Evaluation> findAllByTerm(String term);
}
