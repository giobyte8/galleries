package me.giobyte8.galleries.scanner.dto;

import java.util.UUID;

public record FileDiscoveryEvent(
        UUID scanRequestId,
        FDiscoveryEventType eventType,
        String filePath
) {
}
