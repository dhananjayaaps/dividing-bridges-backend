package org.penpal.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DistrictSchoolHierarchy {
    private String district;
    private List<SchoolData> schools = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SchoolData {
        private String schoolName;
    }
}

