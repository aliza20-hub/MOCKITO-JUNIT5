package com.WEST.complaintdesk.dto.response;

import com.WEST.complaintdesk.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private Long userId;
    private String fullName;
    private String email;
    private Role role;
}
