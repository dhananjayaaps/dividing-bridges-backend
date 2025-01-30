package org.penpal.payment.model;

import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Document(collection = "payment")
public class Payment {
    @Id
    String id;
    String email;
    String name;
    String bankName;
    String accountNumber;
    String branch;

    public Payment(String email, String name, String bankName, String accountNumber, String branch) {
        this.email = email;
        this.name = name;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.branch = branch;
    }
}
