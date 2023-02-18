package me.giobyte8.galleries.scanner.dto;

import java.util.Date;
import java.util.UUID;

public record ScanOrder(UUID id, String dirHPath, Date requestedAt) {
}
