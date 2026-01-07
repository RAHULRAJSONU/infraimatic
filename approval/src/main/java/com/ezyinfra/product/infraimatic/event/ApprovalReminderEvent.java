package com.ezyinfra.product.infraimatic.event;

import com.ezyinfra.product.infraimatic.data.entity.ApprovalTask;

public record ApprovalReminderEvent(ApprovalTask task) {}