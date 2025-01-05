package org.penpal.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TeacherFilter {
    private String district;
    private String school;
    private String assignedClass;
}
