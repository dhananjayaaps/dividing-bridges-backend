package org.penpal.invitation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InvitationFilter {
    String studentEmail;
    String teacherEmail;
    String invitationStatus;
}
