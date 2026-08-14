package com.WEST.complaintdesk.repository;

import com.WEST.complaintdesk.entity.Role;
import com.WEST.complaintdesk.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByRoleAndDepartmentId(Role role, Long departmentId);
}
