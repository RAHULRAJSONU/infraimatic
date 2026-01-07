package com.ezyinfra.product.infraimatic.service;

import com.ezyinfra.product.infraimatic.data.dto.ApprovalActionResponseDto;
import com.ezyinfra.product.infraimatic.data.dto.ApprovalInstanceStatus;
import com.ezyinfra.product.infraimatic.data.dto.ApprovalTaskStatus;
import com.ezyinfra.product.infraimatic.data.entity.ApprovalInstance;
import com.ezyinfra.product.infraimatic.data.entity.ApprovalLevel;
import com.ezyinfra.product.infraimatic.data.entity.ApprovalTask;
import com.ezyinfra.product.infraimatic.data.mapper.ApprovalResponseMapper;
import com.ezyinfra.product.infraimatic.data.repository.ApprovalInstanceRepository;
import com.ezyinfra.product.infraimatic.event.ApprovalApprovedEvent;
import com.ezyinfra.product.infraimatic.event.ApprovalRejectedEvent;
import com.ezyinfra.product.infraimatic.exception.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@Transactional
public class ApprovalActionService {

    private final ApprovalInstanceRepository instanceRepo;
    private final ApplicationEventPublisher publisher;

    public ApprovalActionService(
            ApprovalInstanceRepository instanceRepo,
            ApplicationEventPublisher publisher) {
        this.instanceRepo = instanceRepo;
        this.publisher = publisher;
    }

    @Transactional
    public ApprovalActionResponseDto act(
            UUID instanceId,
            String actor,
            Set<String> roles,
            boolean approve,
            String comment) {

        try {
            // -------------------------------------------------
            // 1️⃣ Load instance
            // -------------------------------------------------
            ApprovalInstance instance = instanceRepo.findById(instanceId)
                    .orElseThrow(() ->
                            new ApprovalNotFoundException(instanceId.toString()));

            if (instance.getStatus() != ApprovalInstanceStatus.PENDING) {
                throw new ApprovalAlreadyCompletedException();
            }

            // SAFE logging (NO entities)
            log.info(
                    "Approval action requested | instanceId={}, actor={}, approve={}",
                    instance.getId(), actor, approve
            );

            // -------------------------------------------------
            // 2️⃣ Find THIS USER’s pending task
            // -------------------------------------------------
            ApprovalTask myTask = instance.getTasks().stream()
                    .filter(t -> t.getStatus() == ApprovalTaskStatus.PENDING)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No pending approval task assigned to user"));

            // -------------------------------------------------
            // 3️⃣ Act on the task
            // -------------------------------------------------
            myTask.setStatus(
                    approve
                            ? ApprovalTaskStatus.APPROVED
                            : ApprovalTaskStatus.REJECTED
            );
            myTask.setActedAt(Instant.now());
            myTask.setActedBy(actor);
            myTask.setComment(comment);

            // -------------------------------------------------
            // 4️⃣ Immediate REJECT → terminate approval
            // -------------------------------------------------
            if (!approve) {
                instance.setStatus(ApprovalInstanceStatus.REJECTED);
                instance.setLocked(false);
                instance.setUpdatedAt(Instant.now());

                ApprovalInstance saved = instanceRepo.save(instance);
                publisher.publishEvent(new ApprovalRejectedEvent(saved));
                return ApprovalResponseMapper.toResponse(saved);
            }

            // -------------------------------------------------
            // 5️⃣ Handle group completion logic
            // -------------------------------------------------
            int levelOrder = myTask.getLevelOrder();
            int group = myTask.getApprovalGroup();

            List<ApprovalTask> sameGroupTasks =
                    instance.getTasks().stream()
                            .filter(t ->
                                    t.getLevelOrder() == levelOrder &&
                                            t.getApprovalGroup() == group)
                            .toList();

            ApprovalLevel levelPolicy =
                    instance.getTemplate()
                            .getLevels()
                            .stream()
                            .filter(l -> l.getLevelOrder() == levelOrder)
                            .findFirst()
                            .orElseThrow(() ->
                                    new ApprovalInvalidStateException(
                                            "Approval level not found"));

            boolean groupCompleted =
                    levelPolicy.isRequireAllApprovals()
                            ? sameGroupTasks.stream()
                            .allMatch(t ->
                                    t.getStatus() == ApprovalTaskStatus.APPROVED)
                            : sameGroupTasks.stream()
                            .anyMatch(t ->
                                    t.getStatus() == ApprovalTaskStatus.APPROVED);

            if (groupCompleted) {
                activateNextGroup(instance, group);
            }

            // -------------------------------------------------
            // 6️⃣ Check if approval is fully complete
            // -------------------------------------------------
            boolean anyPending =
                    instance.getTasks().stream()
                            .anyMatch(t ->
                                    t.getStatus() == ApprovalTaskStatus.PENDING);

            if (!anyPending) {
                instance.setStatus(ApprovalInstanceStatus.APPROVED);
                instance.setLocked(false);
                instance.setUpdatedAt(Instant.now());

                ApprovalInstance saved = instanceRepo.save(instance);
                publisher.publishEvent(new ApprovalApprovedEvent(saved));
                return ApprovalResponseMapper.toResponse(saved);
            }

            // -------------------------------------------------
            // 7️⃣ Persist intermediate state
            // -------------------------------------------------
            instance.setUpdatedAt(Instant.now());
            return ApprovalResponseMapper.toResponse(instanceRepo.save(instance));

        } catch (OptimisticLockingFailureException e) {
            throw new ApprovalConcurrencyException();
        }
    }

    /**
     * Activate the next approval group.
     * Tasks are already created; only BLOCKED → PENDING transition is needed.
     */
    private void activateNextGroup(
            ApprovalInstance instance,
            int currentGroup) {

        OptionalInt nextGroupOpt =
                instance.getTasks().stream()
                        .mapToInt(ApprovalTask::getApprovalGroup)
                        .filter(g -> g > currentGroup)
                        .min();

        if (nextGroupOpt.isEmpty()) {
            return; // no next group
        }

        int nextGroup = nextGroupOpt.getAsInt();

        for (ApprovalTask task : instance.getTasks()) {
            if (task.getApprovalGroup() == nextGroup &&
                    task.getStatus() == ApprovalTaskStatus.BLOCKED) {

                task.setStatus(ApprovalTaskStatus.PENDING);
            }
        }
    }

    /**
     * Authorization check for task ownership.
     */
    private boolean isAuthorized(
            ApprovalTask task,
            String actor,
            Set<String> roles) {

        if ("USER".equals(task.getApproverType())) {
            return task.getApprover().equals(actor);
        }

        // ROLE based approval
        return roles.contains(task.getApprover());
    }
}
