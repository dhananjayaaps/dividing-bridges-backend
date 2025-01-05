package org.penpal.invitation.model;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.penpal.invitation.dto.Teacher;
import org.penpal.shared.InvitationStatus;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "invitation")
public class Invitation {
    @Id
    private String id;
    private String studentEmail;
    private Teacher teacher;
    private InvitationStatus invitationStatus;
    private LocalDateTime sentAt;

    public Invitation(String studentEmail, Teacher teacher, LocalDateTime sentAt, InvitationStatus invitationStatus) {
        this.studentEmail = studentEmail;
        this.teacher = teacher;
        this.sentAt = sentAt;
        this.invitationStatus = invitationStatus;
    }
}


