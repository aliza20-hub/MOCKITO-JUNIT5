package com.WEST.complaintdesk.dto.request;

import com.WEST.complaintdesk.entity.ComplaintCategory;
import com.WEST.complaintdesk.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ComplaintRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Category is required")
    private ComplaintCategory category;

    private String location;

    private Priority priority; // optional, defaults to MEDIUM in service

    // student can hint which department this belongs to, admin can override later during assignment
    private Long departmentId;
}
