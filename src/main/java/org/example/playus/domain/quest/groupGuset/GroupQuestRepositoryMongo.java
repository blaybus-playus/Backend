package org.example.playus.domain.quest.groupGuset;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupQuestRepositoryMongo extends MongoRepository<GroupQuest, String> {

    List<GroupQuest> findAllByAffiliationAndDepartment(String affiliation, int department);

    Optional<GroupQuest> findByAffiliationAndDepartment(String affiliation, int department);
}
