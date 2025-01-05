package org.penpal.email.service;

import org.penpal.email.dto.MessageFilter;
import org.penpal.email.dto.MessagePayload;
import org.penpal.email.dto.Student;
import org.penpal.email.dto.StudentActivityResponse;
import org.penpal.email.model.EmailThread;
import org.penpal.email.repository.EmailThreadRepository;
import org.penpal.email.service.dto.EmailSendingPayload;
import org.penpal.email.service.service.EmailSendingService;
import org.penpal.shared.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmailThreadService {

    @Autowired
    private EmailThreadRepository emailThreadRepository;

    @Autowired
    private EmailSendingService emailSendingService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${auth.service.hosted.url}")
    private String AUTH_SERVICE_HOSTED_URL;

    public ResponseEntity<?> createThread(String sender, String recipient, String body,
                                          String language, String type, List<MultipartFile> attachments) {
        EmailThread thread = new EmailThread();
        thread.setThreadId(UUID.randomUUID().toString());
        thread.setParticipants(List.of(sender, recipient));
        List<EmailThread.Message.Attachment> attachedAttachments = null;
        if (attachments != null && !attachments.isEmpty()) {
            attachedAttachments = attachments.stream()
                    .map(file -> {
                        try {
                            return new EmailThread.Message.Attachment(
                                    file.getOriginalFilename(),
                                    file.getContentType(),
                                    file.getBytes()
                            );
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to process attachment: " + file.getOriginalFilename(), e);
                        }
                    })
                    .collect(Collectors.toList());
        }

        EmailThread.Message initialMessage = new EmailThread.Message(
                UUID.randomUUID().toString(),
                sender,
                body,
                Objects.equals(type, MessageType.ADMIN.toString()) || Objects.equals(type, MessageType.PENPAL.toString()) ?
                        MessageStatus.PENDING : MessageStatus.APPROVED,
                LocalDateTime.now(),
                language,
                MessageType.valueOf(type)
        );
        initialMessage.setAttachments(attachedAttachments);

        thread.setMessages(List.of(initialMessage));
        thread.setCreatedAt(LocalDateTime.now());
        thread.setUpdatedAt(LocalDateTime.now());
        return new ResponseEntity<EmailThread>(emailThreadRepository.save(thread), HttpStatus.OK);
    }

    public ResponseEntity<?> getThreads() {
        return new ResponseEntity<List<EmailThread>>(emailThreadRepository.findAll(), HttpStatus.OK);
    }

    public ResponseEntity<?> getThread(String threadId) {
        Optional<EmailThread> thread = emailThreadRepository.findByThreadId(threadId);
        if (thread.isPresent()) {
            return new ResponseEntity<>(thread.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(new APIResponse("Thread not found!"), HttpStatus.NOT_FOUND);
    }

//    public ResponseEntity<?> getAttachments(String threadId, String messageId) {
//        Optional<EmailThread> thread = emailThreadRepository.findByThreadId(threadId);
//        if (thread.isPresent()) {
//            Optional<EmailThread.Message> existingMessage = thread.get().getMessages().stream()
//                    .filter(message ->
//                            message.getMessageId().equals(messageId)).findFirst();
//            if (existingMessage.isPresent()) {
//                EmailThread.Message message = existingMessage.get();
//
//                // Check if there are attachments
//                if (message.getAttachments() != null && attachmentIndex < message.getAttachments().size()) {
//                    EmailThread.Message.Attachment attachment = message.getAttachments().get(attachmentIndex);
//                    byte[] fileData = attachment.getFileData();
//                    String fileType = attachment.getFileType();
//                    String fileName = attachment.getFileName();
//
//                    return ResponseEntity.ok()
//                            .contentType(MediaType.parseMediaType(fileType))
//                            .body(fileData);
//                } else {
//                    return new ResponseEntity<>("Attachment not found!", HttpStatus.NOT_FOUND);
//                }
//            }
//        } else {
//            return new ResponseEntity<>(new APIResponse("Thread not found!"), HttpStatus.NOT_FOUND);
//        }
//    }

    public ResponseEntity<?> addMessage(String threadId, String sender, String body, String type,
                                        String language, List<MultipartFile> attachments) throws IOException {
        Optional<EmailThread> thread = emailThreadRepository.findByThreadId(threadId);
        if (thread.isPresent()) {
            EmailThread.Message message = new EmailThread.Message();
            message.setMessageId(UUID.randomUUID().toString());
            message.setSender(sender);
            message.setBody(body);
            message.setStatus(Objects.equals(type, MessageType.ADMIN.toString()) || Objects.equals(type, MessageType.PENPAL.toString()) ?
                    MessageStatus.PENDING : MessageStatus.APPROVED);
            message.setSentAt(LocalDateTime.now());
            message.setLanguage(language);
            message.setType(MessageType.valueOf(type));

            if (attachments != null && !attachments.isEmpty()) {
                List<EmailThread.Message.Attachment> attachedAttachments = attachments.stream()
                        .map(file -> {
                            try {
                                return new EmailThread.Message.Attachment(
                                        file.getOriginalFilename(),
                                        file.getContentType(),
                                        file.getBytes()
                                );
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to process attachment: " + file.getOriginalFilename(), e);
                            }
                        })
                        .collect(Collectors.toList());
                message.setAttachments(attachedAttachments);
            }

            thread.get().getMessages().add(message);
            thread.get().setUpdatedAt(LocalDateTime.now());
            return new ResponseEntity<EmailThread>(emailThreadRepository.save(thread.get()), HttpStatus.OK);
        }
        return new ResponseEntity<>(new APIResponse("Thread not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> moderateMessage(
            String threadId,
            String messageId,
            String moderator,
            String action,
            String reason) {
        Optional<EmailThread> thread = emailThreadRepository.findByThreadId(threadId);
        if (thread.isPresent()) {
            Optional<EmailThread.Message> existingMessage = thread.get().getMessages().stream()
                    .filter(message ->
                            message.getMessageId().equals(messageId)).findFirst();
            if (existingMessage.isPresent()) {
                if (existingMessage.get().getType() == MessageType.ADMIN && !thread.get().getParticipants().contains(moderator))
                    thread.get().getParticipants().add(moderator);

                EmailThread.Message.ModerationRecord record = new EmailThread.Message.ModerationRecord();
                record.setModerator(moderator);
                record.setAction(MessageStatus.valueOf(action));
                record.setReason(reason);
                record.setTimestamp(LocalDateTime.now());
                existingMessage.get().setModerationRecord(record);
                existingMessage.get().setStatus(MessageStatus.valueOf(action));
                thread.get().setUpdatedAt(LocalDateTime.now());
                return new ResponseEntity<EmailThread>(emailThreadRepository.save(thread.get()), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new APIResponse("Message not found!"), HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<>(new APIResponse("Thread not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> getThreadsForParticipant(String participant) {
        return new ResponseEntity<List<EmailThread>>(emailThreadRepository.findByParticipantsContaining(participant), HttpStatus.OK);
    }

    public ResponseEntity<?> getVisibleMessagesForStudents(String threadId, String participant) {
        Optional<EmailThread> thread = emailThreadRepository.findByThreadId(threadId);
        if (thread.isPresent()) {
            if (!thread.get().getParticipants().contains(participant)) {
                return new ResponseEntity<>(new APIResponse("Participant not authorized to view this thread"), HttpStatus.UNAUTHORIZED);
            }
            List<EmailThread.Message> messages = thread.get().getMessages().stream()
                    .filter(message ->
                            message.getSender().equals(participant) || // Own messages
                                    MessageStatus.APPROVED.equals(message.getStatus())) // Approved messages
                    .toList();
            return new ResponseEntity<List<EmailThread.Message>>(messages, HttpStatus.OK);
        }
        return new ResponseEntity<>(new APIResponse("Thread not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> filterMessages(String threadId, MessageFilter filter) {
        Optional<EmailThread> thread = emailThreadRepository.findByThreadId(threadId);
        if (thread.isPresent()) {
            List<EmailThread.Message> messages = thread.get().getMessages().stream()
                    .filter(message ->
                            (filter.getSender() == null || message.getSender().equals(filter.getSender())) &&
                                    (filter.getStatus() == null || message.getStatus().equals(MessageStatus.valueOf(filter.getStatus()))) &&
                                    (filter.getLanguage() == null || message.getLanguage().equals(filter.getLanguage())) &&
                                    (filter.getType() == null || message.getType().equals(MessageType.valueOf(filter.getType())))
                    )
                    .toList();
            return new ResponseEntity<List<EmailThread.Message>>(messages, HttpStatus.OK);
        }
        return new ResponseEntity<>(new APIResponse("Thread not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> updateMessage(String threadId, String messageId, MessagePayload messagePayload) {
        Optional<EmailThread> thread = emailThreadRepository.findByThreadId(threadId);
        if (thread.isPresent()) {
            Optional<EmailThread.Message> existingMessage = thread.get().getMessages().stream()
                    .filter(message ->
                            message.getMessageId().equals(messageId)).findFirst();
            if (existingMessage.isPresent()) {
                if (messagePayload.getBody() != null) {
                    existingMessage.get().setBody(messagePayload.getBody());
                }
                if (messagePayload.getLanguage() != null) {
                    existingMessage.get().setLanguage(messagePayload.getLanguage());
                }
            } else {
                return new ResponseEntity<>(new APIResponse("Message not found!"), HttpStatus.NOT_FOUND);
            }
            thread.get().setUpdatedAt(LocalDateTime.now());
            return new ResponseEntity<EmailThread>(emailThreadRepository.save(thread.get()), HttpStatus.OK);
        }
        return new ResponseEntity<>(new APIResponse("Thread not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> deleteMessage(String threadId, String messageId) {
        Optional<EmailThread> thread = emailThreadRepository.findByThreadId(threadId);
        if (thread.isPresent()) {
            Optional<EmailThread.Message> existingMessage = thread.get().getMessages().stream()
                    .filter(message ->
                            message.getMessageId().equals(messageId)).findFirst();
            if (existingMessage.isPresent()) {
                boolean removed = thread.get().getMessages().removeIf(message -> messageId.equals(message.getMessageId()));
                if (!removed) {
                    return new ResponseEntity<>(new APIResponse("Message id does not match!"), HttpStatus.BAD_REQUEST);
                }
                thread.get().setUpdatedAt(LocalDateTime.now());
                return new ResponseEntity<>(emailThreadRepository.save(thread.get()), HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(new APIResponse("Message id not found!"), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(new APIResponse("Thread not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> deleteThread(String threadId) {
        Optional<EmailThread> thread = emailThreadRepository.findByThreadId(threadId);
        if (thread.isPresent()) {
            emailThreadRepository.delete(thread.get());
            return new ResponseEntity<>(new APIResponse("Thread deleted"), HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(new APIResponse("Thread not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> getInactiveStudents() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime startOfLastWeek = now.minusWeeks(1).with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDateTime endOfLastWeek = now.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY));

        Student[] allStudents = restTemplate.getForObject(AUTH_SERVICE_HOSTED_URL + "api/auth/students", Student[].class);

        Set<String> activeStudentEmails = emailThreadRepository.findByMessagesSentAtBetween(startOfLastWeek, endOfLastWeek)
                .stream()
                .flatMap(thread -> thread.getMessages().stream())
                .map(EmailThread.Message::getSender)
                .collect(Collectors.toSet());

        assert allStudents != null;
        List<Student> inactiveStudents = Arrays.stream(allStudents)
                .filter(student -> !activeStudentEmails.contains(student.getUserEmail()))
                .toList();

        return new ResponseEntity<List<Student>>(inactiveStudents, HttpStatus.OK);
    }

    public ResponseEntity<?> remindInactiveStudents() {
        ResponseEntity<?> inactiveStudentsResponse = getInactiveStudents();

        if (inactiveStudentsResponse != null && inactiveStudentsResponse.getBody() instanceof List<?>) {
            List<Student> inactiveStudents = (List<Student>) inactiveStudentsResponse.getBody();

            for (Student student : inactiveStudents) {
                String updateUrl = AUTH_SERVICE_HOSTED_URL + "api/auth/students/" + student.getUserEmail() + "/account-status?status=" + AccountStatus.INACTIVE;
                restTemplate.put(updateUrl, null);
            }

            EmailSendingPayload emailSendingPayload = new EmailSendingPayload();
            emailSendingPayload.setSubject(EmailTemplates.STUDENT_INVITATION_SUBJECT);
            emailSendingPayload.setBody(EmailTemplates.REMINDER_EMAIL_BODY);

            try {
                for (Student student : inactiveStudents) {
                    emailSendingPayload.setTo(student.getUserEmail());
                    emailSendingService.sendEmail(emailSendingPayload);
                }
                return new ResponseEntity<>(new APIResponse("Emails sent successfully"), HttpStatus.OK);
            } catch (MailException ex) {
                return new ResponseEntity<>(new APIResponse("Failed to send email! Error: " + ex.getMessage()),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<>(new APIResponse("No inactive students found or failed to fetch inactive students!"),
                HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> getStudentActivityForLastWeek() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime startOfLastWeek = now.minusWeeks(1).with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDateTime endOfLastWeek = now.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SATURDAY));

        Student[] allStudents = restTemplate.getForObject(AUTH_SERVICE_HOSTED_URL + "api/auth/students", Student[].class);

        Map<String, Long> emailCounts = emailThreadRepository.findByMessagesSentAtBetween(startOfLastWeek, endOfLastWeek)
                .stream()
                .flatMap(thread -> thread.getMessages().stream())
                .filter(message -> message.getSentAt().isAfter(startOfLastWeek) && message.getSentAt().isBefore(endOfLastWeek))
                .collect(Collectors.groupingBy(EmailThread.Message::getSender, Collectors.counting()));

        assert allStudents != null;
        List<StudentActivityResponse> activityResponses = Arrays.stream(allStudents).toList().stream()
                .map(student -> {
                    Long activityCount = emailCounts.getOrDefault(student.getUserEmail(), 0L);
                    return new StudentActivityResponse(
                            student.getUserFullName(),
                            student.getSchool(),
                            student.getGrade(),
                            activityCount > 0 ? activityCount.toString() : "None"
                    );
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(activityResponses, HttpStatus.OK);
    }

    public ResponseEntity<?> getEmailsSentToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        long emailCount = emailThreadRepository.findByMessagesSentAtBetween(startOfDay, endOfDay)
                .stream()
                .flatMap(thread -> thread.getMessages().stream())
                .filter(message -> message.getSentAt().isAfter(startOfDay) && message.getSentAt().isBefore(endOfDay))
                .count();

        return new ResponseEntity<>(new APIResponse(String.valueOf(emailCount)), HttpStatus.OK);
    }

}
