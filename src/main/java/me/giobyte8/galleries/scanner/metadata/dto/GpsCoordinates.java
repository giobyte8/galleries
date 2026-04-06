package me.giobyte8.galleries.scanner.metadata.dto;

import lombok.Builder;

@Builder
public record GpsCoordinates(Double latitude, Double longitude) {
}
