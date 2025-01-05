package org.penpal.auth.repository;

import org.penpal.auth.model.Student;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends MongoRepository<Student,String> {
    Optional<Student> findByUserEmail(String userEmail);
    List<Student> findByAssignedClass(String assignedClass);
    Optional<Student> findByPenpalEmail(String userEmail);
}