package org.penpal.email.controller;

import lombok.RequiredArgsConstructor;
import org.penpal.email.dto.MessageFilter;
import org.penpal.email.dto.MessagePayload;
import org.penpal.email.service.EmailThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email-threads")
public class EmailThreadController {

    @Autowired
    private EmailThreadService emailThreadService;

    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> createThread(
            @RequestPart("subject") String subject,
            @RequestPart("sender") String sender,
            @RequestPart("recipient") String recipient,
            @RequestPart("body") String body,
            @RequestPart("language") String language,
            @RequestPart("type") String type,
            @RequestPart("isDraft") String isDraft,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {
        return emailThreadService.createThread(subject, sender, recipient, body, language, type, isDraft, attachments);
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getThreads() {
        return emailThreadService.getThreads();
    }

    @GetMapping("/{threadId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getThread(@PathVariable("threadId") String threadId) {
        return emailThreadService.getThread(threadId);
    }

    @PostMapping("/{threadId}/add-message")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> addMessage(
            @PathVariable("threadId") String threadId,
            @RequestPart("sender") String sender,
            @RequestPart("body") String body,
            @RequestPart("type") String type,
            @RequestPart("language") String language,
            @RequestPart("isDraft") String isDraft,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) throws IOException {
        return emailThreadService.addMessage(threadId, sender, body, type, language, isDraft, attachments);
    }

    @PutMapping("/{threadId}/messages/{messageId}/moderate")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> moderateMessage(
            @PathVariable("threadId") String threadId,
            @PathVariable("messageId") String messageId,
            @RequestParam("moderator") String moderator,
            @RequestParam("action") String action,
            @RequestBody(required = false) String reason) {
        return emailThreadService.moderateMessage(threadId, messageId, moderator, action, reason);
    }

    @GetMapping("/participants/{participant}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getThreadsForParticipant(@PathVariable("participant") String participant) {
        return emailThreadService.getThreadsForParticipant(participant);
    }

    @GetMapping("/{threadId}/visible-messages")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getVisibleMessagesForStudents(
            @PathVariable("threadId") String threadId, @RequestParam("participant") String participant) {
        return emailThreadService.getVisibleMessagesForStudents(threadId, participant);
    }

    @PostMapping("/{threadId}/filter-messages")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> filterMessages(
            @PathVariable("threadId") String threadId,
            @RequestBody MessageFilter filter) {
        return emailThreadService.filterMessages(threadId, filter);
    }

    @PatchMapping("/{threadId}/messages/{messageId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateMessage(
            @PathVariable("threadId") String threadId,
            @PathVariable("messageId") String messageId,
            @RequestBody MessagePayload messagePayload) {
        return emailThreadService.updateMessage(threadId, messageId, messagePayload);
    }

    @DeleteMapping("/{threadId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteThread(@PathVariable("threadId") String threadId) {
        return emailThreadService.deleteThread(threadId);
    }

    @DeleteMapping("/{threadId}/messages/{messageId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteMessage(
            @PathVariable("threadId") String threadId,
            @PathVariable("messageId") String messageId) {
        return emailThreadService.deleteMessage(threadId, messageId);
    }

    @GetMapping("/inactive-students")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getInactiveStudents() {
        return emailThreadService.getInactiveStudents();
    }

    @GetMapping("/inactive-students/reminder")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> remindInactiveStudents() {
        return emailThreadService.remindInactiveStudents();
    }

    @GetMapping("/student-activity")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getStudentActivityForLastWeek() {
        return emailThreadService.getStudentActivityForLastWeek();
    }

    @GetMapping("/emails-sent-today")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getEmailsSentToday() {
        return emailThreadService.getEmailsSentToday();
    }
}
