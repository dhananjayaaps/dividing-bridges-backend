package org.penpal.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StudentFilter {
    String accountDeleteStatus;
    String school;
    String district;
    String teacherName;
    String grade;
    String gender;
    String preferredLanguage;
    String accountStatus;
    String penpalGroup;
}
