package org.penpal.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.penpal.shared.UserRole;

@Getter
@Setter
@NoArgsConstructor
public class TeacherResponse {
    private String id;
    private String userEmail;
    private String userFullName;
    private UserRole userRole;
    private String district;
    private String school;
    private String assignedClass;
    private String contactNumber;
}
