package org.example.playus.domain.admin;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepositoryMongo extends MongoRepository<Admin, String> {
}
