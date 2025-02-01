package org.penpal.invitation.controller;

import lombok.RequiredArgsConstructor;
import org.penpal.invitation.dto.AdminInvitationPayload;
import org.penpal.invitation.dto.InvitationFilter;
import org.penpal.invitation.dto.InvitationPayload;
import org.penpal.invitation.service.InvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/invitations")
public class InvitationController {
    @Autowired
    private InvitationService invitationService;

    @PostMapping(path = "")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> createInvitation(@RequestBody InvitationPayload invitationPayload) {
        return invitationService.createInvitation(invitationPayload);
    }

    @GetMapping(path = "")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getInvitations() {
        return invitationService.getInvitations();
    }

    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getInvitation(@PathVariable("id") String id) {
        return invitationService.getInvitation(id);
    }

    @PostMapping(path = "/filter")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> filterInvitations(@RequestBody InvitationFilter invitationFilter) {
        return invitationService.filterInvitations(invitationFilter);
    }

    @PutMapping(path = "/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateInvitationStatus(@PathVariable("id") String id) {
        return invitationService.updateInvitationStatus(id);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteInvitation(@PathVariable("id") String id) {
        return invitationService.deleteInvitation(id);
    }

    @PostMapping(path = "/admin")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> createAdminInvitation(@RequestBody AdminInvitationPayload invitationPayload) {
        return invitationService.createAdminInvitation(invitationPayload);
    }

    @GetMapping(path = "/admin")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getAdminInvitations() {
        return invitationService.getAdminInvitations();
    }

    @GetMapping(path = "/admin/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getAdminInvitation(@PathVariable("id") String id) {
        return invitationService.getAdminInvitation(id);
    }

    @PostMapping(path = "/admin/filter")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> filterAdminInvitations(@RequestBody InvitationFilter invitationFilter) {
        return invitationService.filterAdminInvitations(invitationFilter);
    }

    @PutMapping(path = "/admin/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateAdminInvitationStatus(@PathVariable("id") String id) {
        return invitationService.updateAdminInvitationStatus(id);
    }

    @DeleteMapping(path = "/admin/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteAdminInvitation(@PathVariable("id") String id) {
        return invitationService.deleteAdminInvitation(id);
    }
}
