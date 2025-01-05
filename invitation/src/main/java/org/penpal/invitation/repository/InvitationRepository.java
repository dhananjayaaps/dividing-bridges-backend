package org.penpal.invitation.repository;

import org.penpal.invitation.model.Invitation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InvitationRepository extends MongoRepository<Invitation, String> {
}