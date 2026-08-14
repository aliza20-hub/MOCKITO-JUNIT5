package com.WEST.complaintdesk.entity;

/**
 * Access levels within the system.
 * STUDENT  -> can raise complaints and track their own
 * STAFF    -> handles complaints assigned to their department
 * ADMIN    -> full access, assigns complaints, manages users/departments
 */
public enum Role {
    STUDENT,
    STAFF,
    ADMIN
}
