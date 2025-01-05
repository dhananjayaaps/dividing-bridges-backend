package org.penpal.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.penpal.auth.model.Student;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DistrictHierarchy {
    private String district;
    private List<SchoolHierarchy> schools = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SchoolHierarchy {
        private String school;
        private List<TeacherHierarchy> teachers = new ArrayList<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TeacherHierarchy {
        private String teacherName;
        private List<StudentResponse> students = new ArrayList<>();
    }

}
