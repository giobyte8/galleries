package me.giobyte8.galleries.persistence.models;

/**
 * Status of a scan operation.
 */
public enum ScanStatus {
    /** Scan is currently in progress */
    IN_PROGRESS,

    /** Scan completed successfully */
    COMPLETED,

    /** Scan failed with an error */
    FAILED
}

