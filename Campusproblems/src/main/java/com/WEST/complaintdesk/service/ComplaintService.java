package com.WEST.complaintdesk.service;

import com.WEST.complaintdesk.dto.request.AssignComplaintRequest;
import com.WEST.complaintdesk.dto.request.ComplaintRequest;
import com.WEST.complaintdesk.dto.request.UpdateStatusRequest;
import com.WEST.complaintdesk.dto.response.ComplaintResponse;
import com.WEST.complaintdesk.entity.*;
import com.WEST.complaintdesk.entity.*;
import com.WEST.complaintdesk.exception.BadRequestException;
import com.WEST.complaintdesk.exception.ForbiddenActionException;
import com.WEST.complaintdesk.exception.ResourceNotFoundException;
import com.WEST.complaintdesk.repository.ComplaintRepository;
import com.WEST.complaintdesk.repository.ComplaintUpdateRepository;
import com.WEST.complaintdesk.repository.DepartmentRepository;
import com.WEST.complaintdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintUpdateRepository complaintUpdateRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    // ---------- CREATE ----------

    @Transactional
    public ComplaintResponse create(ComplaintRequest request, User student) {
        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found with id: " + request.getDepartmentId()));
        }

        Complaint complaint = Complaint.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .location(request.getLocation())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .status(ComplaintStatus.PENDING)
                .submittedBy(student)
                .department(department)
                .build();

        complaint = complaintRepository.save(complaint);

        logUpdate(complaint, student, null, ComplaintStatus.PENDING, "Complaint submitted by " + student.getFullName());

        return ComplaintResponse.from(complaint);
    }

    // ---------- READ / TRACK ----------

    public ComplaintResponse getById(Long id, User requester) {
        Complaint complaint = findEntityById(id);
        assertCanView(complaint, requester);
        return ComplaintResponse.from(complaint);
    }

    /**
     * Central place that decides what a given user is allowed to see:
     * - STUDENT  -> only their own complaints
     * - STAFF    -> complaints assigned to them, or unassigned ones in their department
     * - ADMIN    -> everything
     */
    public List<ComplaintResponse> getVisibleComplaints(User requester) {
        List<Complaint> complaints;

        switch (requester.getRole()) {
            case STUDENT -> complaints = complaintRepository.findBySubmittedById(requester.getId());
            case STAFF -> {
                if (requester.getDepartment() == null) {
                    complaints = complaintRepository.findByAssignedToId(requester.getId());
                } else {
                    complaints = complaintRepository.findByDepartmentId(requester.getDepartment().getId());
                }
            }
            case ADMIN -> complaints = complaintRepository.findAll();
            default -> throw new ForbiddenActionException("Unknown role");
        }

        return complaints.stream()
                .map(ComplaintResponse::from)
                .collect(Collectors.toList());
    }

    public List<ComplaintResponse> getByStatus(ComplaintStatus status) {
        return complaintRepository.findByStatus(status).stream()
                .map(ComplaintResponse::from)
                .collect(Collectors.toList());
    }

    // ---------- ASSIGN (admin routes a complaint to a staff member) ----------

    @Transactional
    public ComplaintResponse assign(Long complaintId, AssignComplaintRequest request, User admin) {
        Complaint complaint = findEntityById(complaintId);

        User staff = userRepository.findById(request.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff user not found with id: " + request.getStaffId()));

        if (staff.getRole() != Role.STAFF) {
            throw new BadRequestException("User with id " + request.getStaffId() + " is not a STAFF member");
        }

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            complaint.setDepartment(department);
        } else if (complaint.getDepartment() == null && staff.getDepartment() != null) {
            complaint.setDepartment(staff.getDepartment());
        }

        ComplaintStatus previous = complaint.getStatus();
        complaint.setAssignedTo(staff);
        complaint.setStatus(ComplaintStatus.ASSIGNED);
        complaint = complaintRepository.save(complaint);

        String remarks = request.getRemarks() != null ? request.getRemarks()
                : "Assigned to " + staff.getFullName();
        logUpdate(complaint, admin, previous, ComplaintStatus.ASSIGNED, remarks);

        return ComplaintResponse.from(complaint);
    }

    // ---------- UPDATE STATUS (staff/admin move complaint through its lifecycle) ----------

    @Transactional
    public ComplaintResponse updateStatus(Long complaintId, UpdateStatusRequest request, User actor) {
        Complaint complaint = findEntityById(complaintId);

        if (actor.getRole() == Role.STAFF) {
            boolean isAssignedToActor = complaint.getAssignedTo() != null
                    && complaint.getAssignedTo().getId().equals(actor.getId());
            if (!isAssignedToActor) {
                throw new ForbiddenActionException("You can only update complaints assigned to you");
            }
        }

        ComplaintStatus previous = complaint.getStatus();
        complaint.setStatus(request.getStatus());

        if (request.getStatus() == ComplaintStatus.RESOLVED) {
            complaint.setResolvedAt(LocalDateTime.now());
        }

        complaint = complaintRepository.save(complaint);

        logUpdate(complaint, actor, previous, request.getStatus(), request.getRemarks());

        return ComplaintResponse.from(complaint);
    }

    // ---------- helpers ----------

    public Complaint findEntityById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
    }

    private void assertCanView(Complaint complaint, User requester) {
        if (requester.getRole() == Role.ADMIN) {
            return;
        }
        if (requester.getRole() == Role.STUDENT
                && complaint.getSubmittedBy().getId().equals(requester.getId())) {
            return;
        }
        if (requester.getRole() == Role.STAFF) {
            boolean assignedToMe = complaint.getAssignedTo() != null
                    && complaint.getAssignedTo().getId().equals(requester.getId());
            boolean sameDepartment = requester.getDepartment() != null
                    && complaint.getDepartment() != null
                    && requester.getDepartment().getId().equals(complaint.getDepartment().getId());
            if (assignedToMe || sameDepartment) {
                return;
            }
        }
        throw new ForbiddenActionException("You do not have permission to view this complaint");
    }

    private void logUpdate(Complaint complaint, User actor, ComplaintStatus previous,
                            ComplaintStatus newStatus, String remarks) {
        ComplaintUpdate update = ComplaintUpdate.builder()
                .complaint(complaint)
                .updatedBy(actor)
                .previousStatus(previous)
                .newStatus(newStatus)
                .remarks(remarks)
                .build();
        complaintUpdateRepository.save(update);
    }
}
