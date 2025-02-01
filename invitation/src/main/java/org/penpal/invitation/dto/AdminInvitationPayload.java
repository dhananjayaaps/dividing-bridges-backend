package org.penpal.invitation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminInvitationPayload {
    private String senderEmail;
    private String receiverEmail;
    private String userRole;
}
