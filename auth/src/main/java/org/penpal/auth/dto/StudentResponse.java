package org.penpal.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.penpal.shared.AccountDeleteStatus;
import org.penpal.shared.AccountStatus;
import org.penpal.shared.PenpalGroup;
import org.penpal.shared.UserRole;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class StudentResponse {
    private String id;
    private String userEmail;
    private String penpalEmail;
    private PenpalGroup penpalGroup;
    private String userFullName;
    private UserRole userRole;
    private String school;
    private String district;
    private String grade;
    private String assignedClass;
    private String teacherName;
    private String gender;
    private LocalDate dateOfBirth;
    private String preferredLanguage;
    private Map<String, Object> personalInterests;
    private AccountDeleteStatus accountDeleteStatus;
    private AccountStatus accountStatus;
}
