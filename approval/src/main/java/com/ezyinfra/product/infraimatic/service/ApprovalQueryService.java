package com.ezyinfra.product.infraimatic.service;

import com.ezyinfra.product.infraimatic.data.entity.ApprovalInstance;
import com.ezyinfra.product.infraimatic.data.entity.ApprovalTask;
import com.ezyinfra.product.infraimatic.data.repository.ApprovalInstanceRepository;
import com.ezyinfra.product.infraimatic.data.repository.ApprovalTaskRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ApprovalQueryService {

    private final ApprovalTaskRepository taskRepo;
    private final ApprovalInstanceRepository instanceRepo;

    public ApprovalQueryService(
            ApprovalTaskRepository taskRepo,
            ApprovalInstanceRepository instanceRepo) {
        this.taskRepo = taskRepo;
        this.instanceRepo = instanceRepo;
    }

    public List<ApprovalTask> myPendingApprovals(
            String user, Set<String> roles) {

        List<ApprovalTask> result = new ArrayList<>();
        result.addAll(taskRepo.findPendingForUser(user));
        result.addAll(taskRepo.findPendingForRoles(roles));
        return result;
    }

    public List<ApprovalTask> myPendingApprovals(String user) {
        List<ApprovalTask> result = new ArrayList<>();
        result.addAll(taskRepo.findPendingForUser(user));
        return result;
    }


    public List<ApprovalTask> myApprovalHistory(String user) {
        return taskRepo.findByActedBy(user);
    }

    public Optional<ApprovalInstance> status(
            String entityType, String entityId) {
        return instanceRepo.findActiveByEntity(entityType, entityId);
    }
}
