package org.penpal.invitation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InvitationPayload {
    private String senderEmail;
    private String receiverEmail;
}
