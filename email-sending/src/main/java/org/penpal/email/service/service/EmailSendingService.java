package org.penpal.email.service.service;

import lombok.AllArgsConstructor;
import org.penpal.email.service.dto.EmailSendingPayload;
import org.penpal.shared.APIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailSendingService {
    @Autowired
    private JavaMailSender mailSender;

    public ResponseEntity<?> sendEmail(EmailSendingPayload emailSendingPayload) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailSendingPayload.getTo());
            message.setSubject(emailSendingPayload.getSubject() != null ? emailSendingPayload.getSubject() : "");
            message.setText(emailSendingPayload.getBody() != null ? emailSendingPayload.getBody() : "");
            mailSender.send(message);
            return new ResponseEntity<>(new APIResponse("Email sent successfully!"), HttpStatus.OK);
        } catch (MailException ex) {
            return new ResponseEntity<>(new APIResponse("Failed to send email! Error: " + ex.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
