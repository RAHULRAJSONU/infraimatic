package com.ezyinfra.product.common.enums;

/**
 * Status of a submission. Used to track lifecycle of submissions from initial
 * ingestion to successful persistence and processing.
 */
public enum EntryStatus {
    /**
     * The submission has been accepted but not yet processed.
     */
    PENDING,
    /**
     * The submission has been successfully processed and persisted.
     */
    SUCCESS,
    /**
     * The submission failed validation or processing.
     */
    FAILED;
}