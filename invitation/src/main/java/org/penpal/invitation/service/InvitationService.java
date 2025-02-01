package org.penpal.invitation.service;

import org.penpal.email.service.dto.EmailSendingPayload;
import org.penpal.email.service.service.EmailSendingService;
import org.penpal.invitation.dto.AdminInvitationPayload;
import org.penpal.invitation.dto.InvitationFilter;
import org.penpal.invitation.dto.InvitationPayload;
import org.penpal.invitation.dto.Teacher;
import org.penpal.invitation.model.AdminInvitation;
import org.penpal.invitation.model.Invitation;
import org.penpal.invitation.repository.AdminInvitationRepository;
import org.penpal.invitation.repository.InvitationRepository;
import org.penpal.shared.APIResponse;
import org.penpal.shared.EmailTemplates;
import org.penpal.shared.InvitationStatus;
import org.penpal.shared.UserRole;
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
    private AdminInvitationRepository adminInvitationRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private EmailSendingService emailSendingService;
    @Value("${auth.service.hosted.url}")
    private String AUTH_SERVICE_HOSTED_URL;
    @Value("${fe.hosted.url}")
    private String FE_HOSTED_URL;

    public ResponseEntity<?> createInvitation(InvitationPayload invitationPayload) {
        Invitation newInvitation = new Invitation();
        newInvitation.setId(UUID.randomUUID().toString());
        Teacher teacher = restTemplate.getForObject(
                AUTH_SERVICE_HOSTED_URL+"api/auth/teachers/" + invitationPayload.getSenderEmail(), Teacher.class);
        assert teacher != null;
        newInvitation.setStudentEmail(invitationPayload.getReceiverEmail());
        newInvitation.setTeacher(teacher);
        newInvitation.setInvitationStatus(InvitationStatus.PENDING);
        newInvitation.setSentAt(LocalDateTime.now());
        EmailSendingPayload emailSendingPayload = new EmailSendingPayload();
        emailSendingPayload.setTo(invitationPayload.getReceiverEmail());
        emailSendingPayload.setSubject(EmailTemplates.STUDENT_INVITATION_SUBJECT);
        emailSendingPayload.setBody(String.format(
                EmailTemplates.STUDENT_INVITATION_BODY,
                FE_HOSTED_URL,
                invitationPayload.getSenderEmail(),
                invitationPayload.getSenderEmail())
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
                        invitation -> (invitationFilter.getSenderEmail() == null || Objects.equals(invitation.getTeacher().getUserEmail(), invitationFilter.getSenderEmail())) &&
                                (invitationFilter.getInvitationStatus() == null || invitation.getInvitationStatus().toString().equals(invitationFilter.getInvitationStatus())) &&
                                (invitationFilter.getReceiverEmail() == null || invitation.getStudentEmail().equals(invitationFilter.getReceiverEmail()))
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

    public ResponseEntity<?> createAdminInvitation(AdminInvitationPayload invitationPayload) {
        AdminInvitation newInvitation = new AdminInvitation();
        newInvitation.setId(UUID.randomUUID().toString());
        newInvitation.setReceiverEmail(invitationPayload.getReceiverEmail());
        newInvitation.setSenderEmail(invitationPayload.getSenderEmail());
        newInvitation.setUserRole(UserRole.valueOf(invitationPayload.getUserRole()));
        newInvitation.setInvitationStatus(InvitationStatus.PENDING);
        newInvitation.setSentAt(LocalDateTime.now());

        EmailSendingPayload emailSendingPayload = new EmailSendingPayload();
        emailSendingPayload.setTo(invitationPayload.getSenderEmail());
        emailSendingPayload.setSubject(EmailTemplates.STUDENT_INVITATION_SUBJECT);
        emailSendingPayload.setBody(String.format(
                EmailTemplates.ADMIN_INVITATION_BODY,
                FE_HOSTED_URL,
                invitationPayload.getUserRole(),
                invitationPayload.getSenderEmail(),
                invitationPayload.getSenderEmail())
        );
        emailSendingService.sendEmail(emailSendingPayload);
        return new ResponseEntity<AdminInvitation>(adminInvitationRepository.save(newInvitation), HttpStatus.OK);
    }

    public ResponseEntity<?> getAdminInvitations() {
        return new ResponseEntity<List<AdminInvitation>>(adminInvitationRepository.findAll(), HttpStatus.OK);
    }

    public ResponseEntity<?> getAdminInvitation(String invitationId) {
        Optional<AdminInvitation> invitation = adminInvitationRepository.findById(invitationId);
        if (invitation.isPresent()) {
            return new ResponseEntity<>(invitation.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(new APIResponse("Invitation not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> deleteAdminInvitation(String invitationId) {
        Optional<AdminInvitation> invitation = adminInvitationRepository.findById(invitationId);
        if (invitation.isPresent()) {
            adminInvitationRepository.delete(invitation.get());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(new APIResponse("Invitation not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> updateAdminInvitationStatus(String invitationId) {
        Optional<AdminInvitation> invitation = adminInvitationRepository.findById(invitationId);
        if (invitation.isPresent()) {
            invitation.get().setInvitationStatus(InvitationStatus.ACCEPTED);
            adminInvitationRepository.save(invitation.get());
            return new ResponseEntity<>(adminInvitationRepository.save(invitation.get()), HttpStatus.OK);
        }
        return new ResponseEntity<>(new APIResponse("Invitation not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> filterAdminInvitations(InvitationFilter invitationFilter) {
        List<AdminInvitation> invitations = adminInvitationRepository.findAll();
        List<AdminInvitation> filteredInvitations = invitations.stream().filter(
                        invitation -> (invitationFilter.getSenderEmail() == null || Objects.equals(invitation.getSenderEmail(), invitationFilter.getSenderEmail())) &&
                                (invitationFilter.getInvitationStatus() == null || invitation.getInvitationStatus().toString().equals(invitationFilter.getInvitationStatus())) &&
                                (invitationFilter.getReceiverEmail() == null || invitation.getReceiverEmail().equals(invitationFilter.getReceiverEmail())) &&
                                (invitationFilter.getUserRole() == null || invitation.getUserRole().equals(UserRole.valueOf(invitationFilter.getUserRole())))
                )
                .toList();

        return new ResponseEntity<>(filteredInvitations, HttpStatus.OK);
    }
}
