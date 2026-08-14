package com.WEST.complaintdesk.dto.response;

import com.WEST.complaintdesk.entity.Complaint;
import com.WEST.complaintdesk.entity.ComplaintCategory;
import com.WEST.complaintdesk.entity.ComplaintStatus;
import com.WEST.complaintdesk.entity.Priority;
import com.nitsri.complaintdesk.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
public class ComplaintResponse {
    private Long id;
    private String title;
    private String description;
    private ComplaintCategory category;
    private String location;
    private Priority priority;
    private ComplaintStatus status;

    private Long submittedById;
    private String submittedByName;

    private Long assignedToId;
    private String assignedToName;

    private Long departmentId;
    private String departmentName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    private List<ComplaintUpdateResponse> updates;

    public static ComplaintResponse from(Complaint c) {
        return ComplaintResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .category(c.getCategory())
                .location(c.getLocation())
                .priority(c.getPriority())
                .status(c.getStatus())
                .submittedById(c.getSubmittedBy().getId())
                .submittedByName(c.getSubmittedBy().getFullName())
                .assignedToId(c.getAssignedTo() != null ? c.getAssignedTo().getId() : null)
                .assignedToName(c.getAssignedTo() != null ? c.getAssignedTo().getFullName() : null)
                .departmentId(c.getDepartment() != null ? c.getDepartment().getId() : null)
                .departmentName(c.getDepartment() != null ? c.getDepartment().getName() : null)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .resolvedAt(c.getResolvedAt())
                .updates(c.getUpdates() == null ? null :
                        c.getUpdates().stream()
                                .map(ComplaintUpdateResponse::from)
                                .collect(Collectors.toList()))
                .build();
    }
}
