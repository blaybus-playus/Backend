package org.example.playus.domain.quest.leaderQuest;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaderQuestExpRepository extends MongoRepository<LeaderQuestExp, String> {
    List<LeaderQuestExp> findAllByAffiliation(String affiliation);
}
