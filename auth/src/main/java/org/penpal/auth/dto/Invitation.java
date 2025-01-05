package org.penpal.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.penpal.shared.InvitationStatus;

@Getter
@Setter
@NoArgsConstructor
public class Invitation {
    private String id;
    private String studentEmail;
    private InvitationStatus invitationStatus;
}
