package me.giobyte8.galleries.scanner.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a request to scan a specific path at a given time.
 *
 * @param id          Unique identifier for the scan request.
 * @param path        The base path to be scanned.
 * @param requestedAt The timestamp when the scan was requested.
 */
public record ScanRequest(UUID id, String path, LocalDateTime requestedAt) {
}
