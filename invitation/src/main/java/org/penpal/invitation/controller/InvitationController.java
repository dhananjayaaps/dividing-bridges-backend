package org.penpal.invitation.controller;

import lombok.RequiredArgsConstructor;
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
}
