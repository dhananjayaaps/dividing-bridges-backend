package org.penpal.invitation.service;

import org.penpal.email.service.dto.EmailSendingPayload;
import org.penpal.email.service.service.EmailSendingService;
import org.penpal.invitation.dto.InvitationFilter;
import org.penpal.invitation.dto.InvitationPayload;
import org.penpal.invitation.dto.Teacher;
import org.penpal.invitation.model.Invitation;
import org.penpal.invitation.repository.InvitationRepository;
import org.penpal.shared.APIResponse;
import org.penpal.shared.EmailTemplates;
import org.penpal.shared.InvitationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class InvitationService {
    @Autowired
    private InvitationRepository invitationRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private EmailSendingService emailSendingService;
    @Value("${auth.service.hosted.url}")
    private String AUTH_SERVICE_HOSTED_URL;

    public ResponseEntity<?> createInvitation(InvitationPayload invitationPayload) {
        Invitation newInvitation = new Invitation();
        newInvitation.setId(UUID.randomUUID().toString());
        Teacher teacher = restTemplate.getForObject(
                AUTH_SERVICE_HOSTED_URL+"api/auth/teachers/" + invitationPayload.getTeacherEmail(), Teacher.class);
        assert teacher != null;
        newInvitation.setStudentEmail(invitationPayload.getStudentEmail());
        newInvitation.setTeacher(teacher);
        newInvitation.setInvitationStatus(InvitationStatus.PENDING);
        newInvitation.setSentAt(LocalDateTime.now());
        EmailSendingPayload emailSendingPayload = new EmailSendingPayload();
        emailSendingPayload.setTo(invitationPayload.getStudentEmail());
        emailSendingPayload.setSubject(EmailTemplates.STUDENT_INVITATION_SUBJECT);
        emailSendingPayload.setBody(String.format(
                EmailTemplates.STUDENT_INVITATION_BODY,
                invitationPayload.getTeacherEmail(),
                invitationPayload.getTeacherEmail())
        );
        emailSendingService.sendEmail(emailSendingPayload);
        return new ResponseEntity<Invitation>(invitationRepository.save(newInvitation), HttpStatus.OK);
    }

    public ResponseEntity<?> getInvitations() {
        return new ResponseEntity<List<Invitation>>(invitationRepository.findAll(), HttpStatus.OK);
    }

    public ResponseEntity<?> getInvitation(String invitationId) {
        Optional<Invitation> invitation = invitationRepository.findById(invitationId);
        if (invitation.isPresent()) {
            return new ResponseEntity<>(invitation.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(new APIResponse("Invitation not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> filterInvitations(InvitationFilter invitationFilter) {
        List<Invitation> invitations = invitationRepository.findAll();
        List<Invitation> filteredInvitations = invitations.stream().filter(
                        invitation -> (invitationFilter.getTeacherEmail() == null || Objects.equals(invitation.getTeacher().getUserEmail(), invitationFilter.getTeacherEmail())) &&
                                (invitationFilter.getInvitationStatus() == null || invitation.getInvitationStatus().toString().equals(invitationFilter.getInvitationStatus())) &&
                                (invitationFilter.getStudentEmail() == null || invitation.getStudentEmail().equals(invitationFilter.getStudentEmail()))
                )
                .toList();
        return new ResponseEntity<>(filteredInvitations, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteInvitation(String invitationId) {
        Optional<Invitation> invitation = invitationRepository.findById(invitationId);
        if (invitation.isPresent()) {
            invitationRepository.delete(invitation.get());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(new APIResponse("Invitation not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> updateInvitationStatus(String invitationId) {
        Optional<Invitation> invitation = invitationRepository.findById(invitationId);
        if (invitation.isPresent()) {
            invitation.get().setInvitationStatus(InvitationStatus.ACCEPTED);
            invitationRepository.save(invitation.get());
            return new ResponseEntity<>(invitationRepository.save(invitation.get()), HttpStatus.OK);
        }
        return new ResponseEntity<>(new APIResponse("Invitation not found!"), HttpStatus.NOT_FOUND);
    }
}
