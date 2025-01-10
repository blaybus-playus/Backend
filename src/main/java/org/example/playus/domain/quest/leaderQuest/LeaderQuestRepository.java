package org.example.playus.domain.quest.leaderQuest;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaderQuestRepository extends MongoRepository<LeaderQuestExp, String> {
}
