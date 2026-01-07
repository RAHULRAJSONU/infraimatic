package com.ezyinfra.product.infraimatic.service;

import com.ezyinfra.product.infraimatic.data.dto.*;
import com.ezyinfra.product.infraimatic.data.entity.*;
import com.ezyinfra.product.infraimatic.data.repository.ApprovalAttachmentRepository;
import com.ezyinfra.product.infraimatic.data.repository.ApprovalInstanceRepository;
import com.ezyinfra.product.infraimatic.event.ApprovalCreatedEvent;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ApprovalOrchestratorService {

    private final ApprovalAttachmentRepository attachmentRepo;
    private final ApprovalInstanceRepository instanceRepo;
    private final ApplicationEventPublisher publisher;

    public ApprovalOrchestratorService(
            ApprovalAttachmentRepository attachmentRepo,
            ApprovalInstanceRepository instanceRepo,
            ApplicationEventPublisher publisher) {
        this.attachmentRepo = attachmentRepo;
        this.instanceRepo = instanceRepo;
        this.publisher = publisher;
    }

    // -------------------------------------------------
    // FIXED APPROVAL TRIGGER (FIXED TEMPLATE)
    // -------------------------------------------------
    public Optional<ApprovalInstance> trigger(
            String entityType, String entityId) {

        return attachmentRepo
                .findByEntityTypeAndEntityId(entityType, entityId)
                .map(att -> {
                    ApprovalInstance instance =
                            buildInstance(att.getTemplate(), entityType, entityId);

                    ApprovalInstance saved = instanceRepo.save(instance);
                    publisher.publishEvent(new ApprovalCreatedEvent(saved));
                    return saved;
                });
    }

    // -------------------------------------------------
    // FIXED APPROVAL TRIGGER (RUNTIME APPROVERS)
    // -------------------------------------------------
    @Transactional
    public ApprovalInstance triggerWithRuntimeApprovers(
            ApprovalTriggerRequest req) {

        log.info("Triggering approval (runtime) for {}:{}",
                req.entityType(), req.entityId());

        // 1️⃣ Fetch attachment + template
        ApprovalAttachment attachment =
                attachmentRepo.findByEntityTypeAndEntityId(
                                req.entityType(), req.entityId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No approval attachment found"));

        ApprovalTemplate template = attachment.getTemplate();

        // 2️⃣ Build runtime map (level → users)
        Map<Integer, List<String>> runtimeUsersByLevel =
                req.runtimeApprovers().stream()
                        .collect(Collectors.toMap(
                                RuntimeApproverRequest::levelOrder,
                                RuntimeApproverRequest::users
                        ));

        // 3️⃣ Validate runtime input against template
        for (ApprovalLevel level : template.getLevels()) {

            if (level.getAssignmentStrategy() == AssignmentStrategy.FIXED) {
                continue;
            }

            List<String> users =
                    runtimeUsersByLevel.get(level.getLevelOrder());

            if (users == null || users.isEmpty()) {
                throw new IllegalStateException(
                        "Missing runtime approvers for level "
                                + level.getLevelOrder());
            }

            if (level.getAssignmentStrategy() ==
                    AssignmentStrategy.RUNTIME_SINGLE &&
                    users.size() != 1) {

                throw new IllegalStateException(
                        "Level " + level.getLevelOrder()
                                + " requires exactly one approver");
            }
        }

        // 4️⃣ Create ApprovalInstance (aggregate root)
        ApprovalInstance instance = new ApprovalInstance();
        instance.setEntityType(req.entityType());
        instance.setEntityId(req.entityId());
        instance.setTemplate(template);
        instance.setStatus(ApprovalInstanceStatus.PENDING);
        instance.setLocked(true);
        instance.setCreatedAt(Instant.now());

        // 5️⃣ Create tasks (grouped by level)
        int approvalGroup = 1;

        for (ApprovalLevel level : template.getLevels()) {

            ApprovalTaskStatus initialStatus =
                    approvalGroup == 1
                            ? ApprovalTaskStatus.PENDING
                            : ApprovalTaskStatus.BLOCKED;

            // FIXED approver
            if (level.getAssignmentStrategy() == AssignmentStrategy.FIXED) {

                ApprovalTask task = buildTask(
                        level,
                        instance,
                        approvalGroup,
                        initialStatus,
                        level.getApproverType(),
                        level.getApprover()
                );

                instance.getTasks().add(task);
            }

            // RUNTIME_SINGLE / RUNTIME_MULTI
            else {

                List<String> users =
                        runtimeUsersByLevel.get(level.getLevelOrder());

                for (String user : users) {

                    ApprovalTask task = buildTask(
                            level,
                            instance,
                            approvalGroup,
                            initialStatus,
                            "USER",
                            user
                    );

                    instance.getTasks().add(task);
                }
            }

            approvalGroup++;
        }

        // 🔴 CRITICAL DEBUG LINE (keep for now)
        log.info("ApprovalInstance tasks before save = {}",
                instance.getTasks().size());

        // 6️⃣ Persist aggregate root (CASCADE saves tasks)
        ApprovalInstance saved = instanceRepo.saveAndFlush(instance);

        // 7️⃣ Publish event AFTER persistence
        publisher.publishEvent(new ApprovalCreatedEvent(saved));

        return saved;
    }

    private ApprovalTask buildTask(
            ApprovalLevel level,
            ApprovalInstance instance,
            int group,
            ApprovalTaskStatus status,
            String approverType,
            String approver) {

        ApprovalTask task = new ApprovalTask();
        task.setInstance(instance);               // 🔴 REQUIRED
        task.setLevelOrder(level.getLevelOrder());
        task.setApprovalGroup(group);
        task.setStatus(status);
        task.setApproverType(approverType);
        task.setApprover(approver);

        Instant now = Instant.now();
        task.setDueAt(now.plus(level.getSlaDuration()));
        task.setReminderAt(
                now.plus(level.getSlaDuration()
                        .minus(level.getReminderBefore()))
        );

        return task;
    }


    // -------------------------------------------------
    // TASK FACTORIES (SAFE)
    // -------------------------------------------------

    private ApprovalTask createUserTask(
            ApprovalLevel level,
            ApprovalInstance instance,
            String user,
            int group,
            ApprovalTaskStatus status) {

        ApprovalTask task = baseTask(level, instance, group, status);
        task.setApproverType("USER");
        task.setApprover(user);
        return task;
    }

    private ApprovalTask createFixedTask(
            ApprovalLevel level,
            ApprovalInstance instance,
            int group,
            ApprovalTaskStatus status) {

        ApprovalTask task = baseTask(level, instance, group, status);
        task.setApproverType(level.getApproverType());
        task.setApprover(level.getApprover());
        return task;
    }

    private ApprovalTask baseTask(
            ApprovalLevel level,
            ApprovalInstance instance,
            int group,
            ApprovalTaskStatus status) {

        ApprovalTask task = new ApprovalTask();
        task.setInstance(instance);                 // 🔴 REQUIRED
        task.setLevelOrder(level.getLevelOrder());
        task.setApprovalGroup(group);
        task.setStatus(status);

        Instant now = Instant.now();
        task.setDueAt(now.plus(level.getSlaDuration()));
        task.setReminderAt(
                now.plus(level.getSlaDuration()
                        .minus(level.getReminderBefore()))
        );

        return task;
    }

    // -------------------------------------------------
    // FIXED TEMPLATE FLOW
    // -------------------------------------------------
    private ApprovalInstance buildInstance(
            ApprovalTemplate template,
            String entityType,
            String entityId) {

        ApprovalInstance instance = new ApprovalInstance();
        instance.setEntityType(entityType);
        instance.setEntityId(entityId);
        instance.setTemplate(template);
        instance.setStatus(ApprovalInstanceStatus.PENDING);
        instance.setLocked(true);
        instance.setCreatedAt(Instant.now());

        int group = 1;

        for (ApprovalLevel level : template.getLevels()) {
            ApprovalTask task = baseTask(
                    level,
                    instance,
                    group,
                    group == 1
                            ? ApprovalTaskStatus.PENDING
                            : ApprovalTaskStatus.BLOCKED
            );
            task.setApproverType(level.getApproverType());
            task.setApprover(level.getApprover());
            instance.getTasks().add(task);
            group++;
        }

        return instance;
    }
}
