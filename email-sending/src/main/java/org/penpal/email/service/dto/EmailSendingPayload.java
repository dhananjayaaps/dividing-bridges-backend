package org.penpal.email.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmailSendingPayload {
    private String to;
    private String subject;
    private String body;
}
