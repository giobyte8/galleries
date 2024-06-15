package me.giobyte8.galleries.scanner.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScanEvent(
        UUID scanRequestId,
        ScanEventType eventType,
        LocalDateTime time) {
}
