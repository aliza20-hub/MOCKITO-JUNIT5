package com.WEST.complaintdesk.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a campus department/unit that resolves complaints,
 * e.g. Hostel Maintenance, Electrical Section, IT Services, Mess Committee.
 */
@Entity
@Table(name = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String code; // e.g. ELEC, HOSTEL, IT, MESS

    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "department")
    private Set<User> staffMembers = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "department")
    private Set<Complaint> complaints = new HashSet<>();
}
