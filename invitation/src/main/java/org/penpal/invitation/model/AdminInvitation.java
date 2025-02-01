package org.penpal.invitation.model;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.penpal.shared.InvitationStatus;
import org.penpal.shared.UserRole;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "admin-invitation")
public class AdminInvitation {
    @Id
    private String id;
    private String receiverEmail;
    private String senderEmail;
    private UserRole userRole;
    private InvitationStatus invitationStatus;
    private LocalDateTime sentAt;

    public AdminInvitation(String receiverEmail, String senderEmail, UserRole userRole, LocalDateTime sentAt, InvitationStatus invitationStatus) {
        this.senderEmail = senderEmail;
        this.receiverEmail = receiverEmail;
        this.userRole = userRole;
        this.sentAt = sentAt;
        this.invitationStatus = invitationStatus;
    }
}
