package org.penpal.invitation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Teacher {
    private String userEmail;
    private String userFullName;
    private String school;
    private String assignedClass;
}
