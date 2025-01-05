package org.penpal.auth.repository;

import org.penpal.auth.model.Teacher;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends MongoRepository<Teacher,String> {
    Optional<Teacher> findByUserEmail(String userEmail);
    List<Teacher> findByDistrict(String district);
}
