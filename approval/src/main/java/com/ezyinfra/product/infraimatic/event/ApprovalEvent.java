package com.ezyinfra.product.infraimatic.event;

public sealed interface ApprovalEvent
        permits ApprovalCreatedEvent,
                ApprovalApprovedEvent,
                ApprovalRejectedEvent {}