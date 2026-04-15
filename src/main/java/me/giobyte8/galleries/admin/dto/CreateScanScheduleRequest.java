package me.giobyte8.galleries.admin.dto;

import java.util.UUID;

public record CreateScanScheduleRequest(
        UUID directoryId,
        String schedule,
        String tzOffset,
        boolean enabled
) {
}

