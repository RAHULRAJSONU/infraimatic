package com.ezyinfra.product.infraimatic.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ApprovalEventListener {

    @TransactionalEventListener(
        phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(ApprovalCreatedEvent event) {
        // notify first approver
        // schedule SLA
    }

    @TransactionalEventListener(
        phase = TransactionPhase.AFTER_COMMIT)
    public void onApproved(ApprovalApprovedEvent event) {
        // unlock entity
        // notify requester
    }

    @TransactionalEventListener(
        phase = TransactionPhase.AFTER_COMMIT)
    public void onRejected(ApprovalRejectedEvent event) {
        // notify requester
        // cancel SLA timers
    }
}
