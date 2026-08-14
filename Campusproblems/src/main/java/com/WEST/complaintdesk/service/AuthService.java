package com.WEST.complaintdesk.service;

import com.WEST.complaintdesk.dto.request.LoginRequest;
import com.WEST.complaintdesk.dto.request.RegisterRequest;
import com.WEST.complaintdesk.dto.response.JwtResponse;
import com.WEST.complaintdesk.entity.Department;
import com.WEST.complaintdesk.entity.Role;
import com.WEST.complaintdesk.entity.User;
import com.WEST.complaintdesk.exception.BadRequestException;
import com.WEST.complaintdesk.exception.ResourceNotFoundException;
import com.WEST.complaintdesk.repository.DepartmentRepository;
import com.WEST.complaintdesk.repository.UserRepository;
import com.WEST.complaintdesk.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public JwtResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists");
        }

        Department department = null;
        if (request.getRole() == Role.STAFF) {
            if (request.getDepartmentId() == null) {
                throw new BadRequestException("departmentId is required when registering as STAFF");
            }
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));
        } else if (request.getDepartmentId() != null) {
            // students can optionally tag which department/branch they belong to
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .registrationNumber(request.getRegistrationNumber())
                .department(department)
                .enabled(true)
                .build();

        userRepository.save(user);

        return loginInternal(request.getEmail(), user);
    }

    public JwtResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return loginInternal(request.getEmail(), user);
    }

    private JwtResponse loginInternal(String email, User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(email)
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();

        String token = jwtUtil.generateToken(userDetails);

        return JwtResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
