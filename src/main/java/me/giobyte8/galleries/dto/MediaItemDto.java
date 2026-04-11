package me.giobyte8.galleries.dto;

import me.giobyte8.galleries.persistence.models.MediaFileStatus;

import java.time.Instant;
import java.time.ZonedDateTime;

public record MediaItemDto(
        String path,
        Long version,
        Long fileSize,
        ZonedDateTime captureDateTime,
        Instant captureInstant,
        String rawCaptureDateTime,
        Instant lastModified,
        Double gpsLatitude,
        Double gpsLongitude,
        String cameraMaker,
        String cameraModel,
        MediaFileStatus status,
        String mediaType
) {
}
