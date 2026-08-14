package com.WEST.complaintdesk.dto.response;

import com.WEST.complaintdesk.entity.ComplaintStatus;
import com.WEST.complaintdesk.entity.ComplaintUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ComplaintUpdateResponse {
    private Long id;
    private ComplaintStatus previousStatus;
    private ComplaintStatus newStatus;
    private String remarks;
    private String updatedByName;
    private LocalDateTime createdAt;

    public static ComplaintUpdateResponse from(ComplaintUpdate update) {
        return ComplaintUpdateResponse.builder()
                .id(update.getId())
                .previousStatus(update.getPreviousStatus())
                .newStatus(update.getNewStatus())
                .remarks(update.getRemarks())
                .updatedByName(update.getUpdatedBy().getFullName())
                .createdAt(update.getCreatedAt())
                .build();
    }
}
