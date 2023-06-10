package me.giobyte8.galleries.scanner.dto;

import me.giobyte8.galleries.scanner.model.MediaFile;

public record UpsertDiscoveredFileResult(MediaFile mFile, FDiscoveryEventType dEvent) {
}
