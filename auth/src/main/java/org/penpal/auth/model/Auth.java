package org.penpal.auth.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Auth {
    private String userEmail;
    private String userPassword;
    private String userRole;
}
