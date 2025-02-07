package org.penpal.invitation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InvitationFilter {
    String senderEmail;
    String receiverEmail;
    String invitationStatus;
    String userRole;
}
