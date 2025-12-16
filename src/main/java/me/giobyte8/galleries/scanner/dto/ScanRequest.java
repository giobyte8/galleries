package me.giobyte8.galleries.scanner.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScanRequest(UUID id, String dirPath, LocalDateTime requestedAt) {
}
