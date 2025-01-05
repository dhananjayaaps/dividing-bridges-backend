package org.penpal.email.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessageFilter {
    private String sender;
    private String status;
    private String language;
    private String type;
}
