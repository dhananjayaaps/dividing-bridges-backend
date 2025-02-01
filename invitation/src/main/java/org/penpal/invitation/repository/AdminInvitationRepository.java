package org.penpal.invitation.repository;

import org.penpal.invitation.model.AdminInvitation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AdminInvitationRepository extends MongoRepository<AdminInvitation, String> {
}
