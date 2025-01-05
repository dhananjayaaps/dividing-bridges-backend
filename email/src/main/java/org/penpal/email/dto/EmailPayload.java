package org.penpal.email.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class EmailPayload {
    private String sender;
    private String recipient;
    private String body;
    private String language;
    private List<MultipartFile> attachments;
}
