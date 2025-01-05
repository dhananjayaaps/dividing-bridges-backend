package org.penpal.email.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StudentActivityResponse {
    private String studentName;
    private String school;
    private String grade;
    private String activity;
}
