package com.WEST.complaintdesk.controller;

import com.WEST.complaintdesk.dto.response.UserResponse;
import com.WEST.complaintdesk.entity.Role;
import com.WEST.complaintdesk.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    // used by admin's "assign complaint" screen to list staff, optionally scoped to a department
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/staff")
    public ResponseEntity<List<UserResponse>> getStaff(@RequestParam(required = false) Long departmentId) {
        if (departmentId != null) {
            return ResponseEntity.ok(userService.getStaffByDepartment(departmentId));
        }
        return ResponseEntity.ok(userService.getByRole(Role.STAFF));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> setEnabled(@PathVariable Long id, @RequestParam boolean enabled) {
        userService.setEnabled(id, enabled);
        return ResponseEntity.noContent().build();
    }
}
