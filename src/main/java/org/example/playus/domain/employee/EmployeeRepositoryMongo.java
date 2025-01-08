package org.example.playus.domain.employee;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepositoryMongo extends MongoRepository<Employee, String> {

    Optional<Employee> findByPersonalInfoName(String username);
}
