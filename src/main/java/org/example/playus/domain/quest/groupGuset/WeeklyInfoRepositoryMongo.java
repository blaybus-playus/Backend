package org.example.playus.domain.quest.groupGuset;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeeklyInfoRepositoryMongo extends MongoRepository<WeeklyInfo, String> {
}
