package com.WEST.complaintdesk.dto.request;

import com.WEST.complaintdesk.entity.ComplaintStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotNull(message = "status is required")
    private ComplaintStatus status;

    private String remarks;
}
