package com.WEST.complaintdesk.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignComplaintRequest {

    @NotNull(message = "staffId is required")
    private Long staffId;

    // optional - lets admin re-route to a different department while assigning
    private Long departmentId;

    private String remarks;
}
