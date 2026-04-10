package me.giobyte8.galleries.scanner.dto;

import lombok.Builder;
import me.giobyte8.galleries.persistence.models.Image;
import me.giobyte8.galleries.persistence.models.Video;

import java.time.Instant;

@Builder
public record Fingerprint(Long fileSize, Instant lastModified) {

    public static Fingerprint from(Image image) {
        return Fingerprint.builder()
                .fileSize(image.getFileSize())
                .lastModified(image.getLastModified())
                .build();
    }

    public static Fingerprint from(Video video) {
        return Fingerprint.builder()
                .fileSize(video.getFileSize())
                .lastModified(video.getLastModified())
                .build();
    }
}

