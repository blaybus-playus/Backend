package org.example.playus.domain.employeeExp;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeExpRepository extends MongoRepository<EmployeeExp, String> {
    Optional<EmployeeExp> findByEmployeeId(int employeeId);

    void deleteByYear(int year);

    Optional<EmployeeExp> findByEmployeeIdAndYear(int employeeId, int year);
}
