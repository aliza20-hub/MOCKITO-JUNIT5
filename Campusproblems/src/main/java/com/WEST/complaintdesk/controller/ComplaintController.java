package com.WEST.complaintdesk.controller;

import com.WEST.complaintdesk.dto.request.AssignComplaintRequest;
import com.WEST.complaintdesk.dto.request.ComplaintRequest;
import com.WEST.complaintdesk.dto.request.UpdateStatusRequest;
import com.WEST.complaintdesk.dto.response.ComplaintResponse;
import com.WEST.complaintdesk.entity.ComplaintStatus;
import com.WEST.complaintdesk.entity.User;
import com.WEST.complaintdesk.security.AuthenticatedUserProvider;
import com.WEST.complaintdesk.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    // STUDENT files a new complaint
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping
    public ResponseEntity<ComplaintResponse> create(@Valid @RequestBody ComplaintRequest request) {
        User student = authenticatedUserProvider.getCurrentUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(complaintService.create(request, student));
    }

    // returns complaints scoped to the caller's role:
    // student -> own complaints, staff -> department/assigned complaints, admin -> all
    @GetMapping
    public ResponseEntity<List<ComplaintResponse>> getVisible() {
        User requester = authenticatedUserProvider.getCurrentUser();
        return ResponseEntity.ok(complaintService.getVisibleComplaints(requester));
    }

    // track a single complaint's full status history
    @GetMapping("/{id}")
    public ResponseEntity<ComplaintResponse> getById(@PathVariable Long id) {
        User requester = authenticatedUserProvider.getCurrentUser();
        return ResponseEntity.ok(complaintService.getById(id, requester));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ComplaintResponse>> getByStatus(@PathVariable ComplaintStatus status) {
        return ResponseEntity.ok(complaintService.getByStatus(status));
    }

    // ADMIN routes a complaint to a staff member / department
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/assign")
    public ResponseEntity<ComplaintResponse> assign(@PathVariable Long id,
                                                      @Valid @RequestBody AssignComplaintRequest request) {
        User admin = authenticatedUserProvider.getCurrentUser();
        return ResponseEntity.ok(complaintService.assign(id, request, admin));
    }

    // STAFF (on their own assigned complaints) or ADMIN moves a complaint through its lifecycle
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<ComplaintResponse> updateStatus(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateStatusRequest request) {
        User actor = authenticatedUserProvider.getCurrentUser();
        return ResponseEntity.ok(complaintService.updateStatus(id, request, actor));
    }
}
