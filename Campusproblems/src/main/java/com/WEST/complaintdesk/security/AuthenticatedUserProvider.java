package com.WEST.complaintdesk.security;

import com.WEST.complaintdesk.entity.User;
import com.WEST.complaintdesk.exception.ResourceNotFoundException;
import com.WEST.complaintdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Small convenience bean so controllers/services don't have to keep re-writing
 * "pull email out of SecurityContext, then look up the User row" every time.
 */
@Component
@RequiredArgsConstructor
public class AuthenticatedUserProvider {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged in user not found - session may be stale"));
    }
}
