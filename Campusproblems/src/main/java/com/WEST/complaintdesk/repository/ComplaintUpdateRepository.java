package com.WEST.complaintdesk.repository;

import com.WEST.complaintdesk.entity.ComplaintUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintUpdateRepository extends JpaRepository<ComplaintUpdate, Long> {
    List<ComplaintUpdate> findByComplaintIdOrderByCreatedAtAsc(Long complaintId);
}
