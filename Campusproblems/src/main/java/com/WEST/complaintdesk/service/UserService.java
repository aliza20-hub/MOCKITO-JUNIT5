package com.WEST.complaintdesk.service;

import com.WEST.complaintdesk.dto.response.UserResponse;
import com.WEST.complaintdesk.entity.Role;
import com.WEST.complaintdesk.entity.User;
import com.WEST.complaintdesk.exception.ResourceNotFoundException;
import com.WEST.complaintdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    // used by admins when picking which staff member to assign a complaint to
    public List<UserResponse> getStaffByDepartment(Long departmentId) {
        return userRepository.findByRoleAndDepartmentId(Role.STAFF, departmentId).stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    public UserResponse getById(Long id) {
        return UserResponse.from(findEntityById(id));
    }

    public User findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public void setEnabled(Long id, boolean enabled) {
        User user = findEntityById(id);
        user.setEnabled(enabled);
        userRepository.save(user);
    }
}
