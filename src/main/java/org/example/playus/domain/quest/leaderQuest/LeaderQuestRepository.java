package org.example.playus.domain.quest.leaderQuest;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderQuestRepository extends MongoRepository<LeaderQuest, String> {
    List<LeaderQuest> findAllByAffiliation(String affiliation);

    LeaderQuest findByLeaderQuestList_QuestName(String questName);
}
