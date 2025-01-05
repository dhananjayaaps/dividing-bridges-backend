package org.penpal.email.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Student {
    private String userEmail;
    private String userFullName;
    private String school;
    private String grade;
}
