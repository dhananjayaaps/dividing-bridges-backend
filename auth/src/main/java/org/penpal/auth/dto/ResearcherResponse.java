package org.penpal.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.penpal.shared.UserRole;

@Getter
@Setter
@NoArgsConstructor
public class ResearcherResponse {
    private String id;
    private String userEmail;
    private String userFullName;
    private UserRole userRole;
    private String contactNumber;
}
