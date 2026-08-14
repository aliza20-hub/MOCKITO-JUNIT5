package com.WEST.complaintdesk.repository;

import com.WEST.complaintdesk.entity.Complaint;
import com.WEST.complaintdesk.entity.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // for a student's "my complaints" view
    List<Complaint> findBySubmittedById(Long studentId);

    // for a staff member's "assigned to me" view
    List<Complaint> findByAssignedToId(Long staffId);

    // all complaints routed to a given department (admin/staff department view)
    List<Complaint> findByDepartmentId(Long departmentId);

    List<Complaint> findByStatus(ComplaintStatus status);

    List<Complaint> findByDepartmentIdAndStatus(Long departmentId, ComplaintStatus status);
}
