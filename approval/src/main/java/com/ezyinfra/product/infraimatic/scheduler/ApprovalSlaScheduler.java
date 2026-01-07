package com.ezyinfra.product.infraimatic.scheduler;

import com.ezyinfra.product.infraimatic.data.entity.ApprovalTask;
import com.ezyinfra.product.infraimatic.data.repository.ApprovalTaskRepository;
import com.ezyinfra.product.infraimatic.event.ApprovalReminderEvent;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@EnableScheduling
public class ApprovalSlaScheduler {

    private final ApprovalTaskRepository taskRepo;
    private final ApplicationEventPublisher publisher;

    public ApprovalSlaScheduler(ApprovalTaskRepository taskRepo, ApplicationEventPublisher publisher) {
        this.taskRepo = taskRepo;
        this.publisher = publisher;
    }

    @Scheduled(fixedDelay = 60000) // every minute
    @Transactional
    public void processSla() {

        Instant now = Instant.now();

        // 🔔 reminders
        taskRepo.findRemindersDue(now)
            .forEach(task -> {
                publisher.publishEvent(
                    new ApprovalReminderEvent(task));
            });

        // ⏰ escalations
        taskRepo.findOverdue(now)
            .forEach(task -> {
                escalate(task);
            });
    }

    private void escalate(ApprovalTask task) {
        if (task.isEscalated()) return;

        task.setEscalated(true);
        task.setEscalatedAt(Instant.now());

        task.setApproverType("ROLE");
        task.setApprover(task.getEscalationRole());
    }
}
