package org.penpal.auth.repository;

import org.penpal.auth.model.Translator;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TranslatorRepository extends MongoRepository<Translator,String> {
    Optional<Translator> findByUserEmail(String userEmail);
}

