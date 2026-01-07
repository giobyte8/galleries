package me.giobyte8.galleries.scanner.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ThumbnailsRequest(
    UUID requestId,
    String filePath
) {
    public ThumbnailsRequest {
        if (requestId == null) {
            requestId = UUID.randomUUID();
        }
    }
}
