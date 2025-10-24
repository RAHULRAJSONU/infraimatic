package com.ezyinfra.product.domain;

/**
 * Types of actions that can be audited. The audit log records changes to
 * entities with these actions.
 */
public enum AuditActionType {
    CREATE,
    UPDATE,
    DELETE;
}