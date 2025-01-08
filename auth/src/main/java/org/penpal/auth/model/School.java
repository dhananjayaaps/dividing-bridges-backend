package org.penpal.auth.model;

import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Document(collection = "school")
public class School {
    @Id
    private String id;
    private String name;
    private String district;

    public School(String name, String district) {
        this.name = name;
        this.district = district;
    }
}
