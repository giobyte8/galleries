package me.giobyte8.galleries.scanner.dto;

import java.util.Calendar;
import java.util.UUID;

public record ScanEvent(
        UUID scanRequestId,
        ScanEventType eventType,
        Calendar time) {
}
