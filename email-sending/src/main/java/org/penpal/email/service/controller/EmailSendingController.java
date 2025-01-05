package org.penpal.email.service.controller;

import lombok.RequiredArgsConstructor;
import org.penpal.email.service.dto.EmailSendingPayload;
import org.penpal.email.service.service.EmailSendingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/email-sending")
public class EmailSendingController {
    @Autowired
    private final EmailSendingService emailSendingService;

    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> sendEmail(@RequestBody EmailSendingPayload emailSendingPayload){
        return emailSendingService.sendEmail(emailSendingPayload);
    }
}
