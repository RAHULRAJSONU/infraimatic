package com.ezyinfra.product.infraimatic.data.repository;

import com.ezyinfra.product.infraimatic.data.entity.ApprovalTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface ApprovalTaskRepository extends JpaRepository<ApprovalTask, Long> {
    @Query("""
        select t from ApprovalTask t
        where t.status = 'PENDING'
        and t.approverType = 'USER'
        and t.approver = :user
    """)
    List<ApprovalTask> findPendingForUser(@Param("user") String user);

    // Pending approvals by role
    @Query("""
        select t from ApprovalTask t
        where t.status = 'PENDING'
        and t.approverType = 'ROLE'
        and t.approver in :roles
    """)
    List<ApprovalTask> findPendingForRoles(@Param("roles") Set<String> roles);

    // Tasks acted by user
    List<ApprovalTask> findByActedBy(String user);

    @Query("""
        select t from ApprovalTask t
        where t.status='PENDING'
        and t.reminderAt <= :now
        """
    )
    List<ApprovalTask> findRemindersDue(Instant now);

    @Query("""
        select t from ApprovalTask t
        where t.status='PENDING'
        and t.dueAt <= :now
        and t.escalated=false
        """
    )
    List<ApprovalTask> findOverdue(Instant now);

}