package me.giobyte8.galleries.admin.dto;

public record UpdateScanScheduleRequest(
        String schedule,
        String tzOffset,
        boolean enabled
) {
}

