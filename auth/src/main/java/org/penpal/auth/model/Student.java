package org.penpal.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.penpal.shared.AccountDeleteStatus;
import org.penpal.shared.AccountStatus;
import org.penpal.shared.PenpalGroup;
import org.penpal.shared.UserRole;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Document(collection = "student")
public class Student{
    @Id
    private String id;
    @Indexed(unique = true)
    private String userEmail;
    private String penpalEmail;
    private PenpalGroup penpalGroup;
    private String password;
    private String userFullName;
    @Enumerated(EnumType.STRING)
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
    private byte[] profilePicture;
    private AccountDeleteStatus accountDeleteStatus;
    private AccountStatus accountStatus;

    public Student(String userEmail, String userFullName, String penpalEmail, String password, String school, String district, String grade, String assignedClass, String teacherName, String gender, LocalDate dateOfBirth, String preferredLanguage, Map<String, Object> personalInterests) {
        this.userEmail = userEmail;
        this.userFullName = userFullName;
        this.penpalEmail = penpalEmail;
        this.password = password;
        this.userRole = UserRole.STUDENT;
        this.school = school;
        this.district = district;
        this.grade = grade;
        this.assignedClass = assignedClass;
        this.teacherName = teacherName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.preferredLanguage = preferredLanguage;
        this.personalInterests = personalInterests;
        this.accountDeleteStatus = AccountDeleteStatus.NONE;
        this.accountStatus = AccountStatus.PENDING;
    }
}
