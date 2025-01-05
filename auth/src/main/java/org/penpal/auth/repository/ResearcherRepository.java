package org.penpal.auth.repository;

import org.penpal.auth.model.Researcher;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResearcherRepository extends MongoRepository<Researcher, String> {
    Optional<Researcher> findByUserEmail(String userEmail);
}

