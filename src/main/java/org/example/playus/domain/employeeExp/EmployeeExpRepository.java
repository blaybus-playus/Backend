package org.example.playus.domain.employeeExp;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeExpRepository extends MongoRepository<EmployeeExp, String> {
}
