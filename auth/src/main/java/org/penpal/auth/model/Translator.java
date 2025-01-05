package org.penpal.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.penpal.shared.UserRole;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
@Document(collection = "translator")
public class Translator  {
    @Id
    private String id;
    @Indexed(unique = true)
    private String userEmail;
    @JsonIgnore
    private String userPassword;
    private String userFullName;
    @Enumerated(EnumType.STRING)
    private UserRole userRole;
    private String contactNumber;

    public Translator(String userEmail, String userFullName, String userPassword, String contactNumber) {
        this.userEmail = userEmail;
        this.userFullName = userFullName;
        this.userPassword = userPassword;
        this.userRole = UserRole.TRANSLATOR;
        this.contactNumber = contactNumber;
    }
}
