package me.giobyte8.galleries.admin.dto;

import me.giobyte8.galleries.persistence.models.ScanStats;
import me.giobyte8.galleries.persistence.models.ScanStatus;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * View-layer wrapper for ScanStats that includes a pre-formatted
 * duration string, computed server-side to keep templates dumb.
 */
public record ScanStatsView(ScanStats stats, String duration) {

    public static ScanStatsView from(ScanStats stats) {
        return new ScanStatsView(stats, formatDuration(stats));
    }

    private static String formatDuration(ScanStats stats) {
        if (stats.getStatus() == ScanStatus.IN_PROGRESS) {

            // Duration is rendered live by Alpine on the client side
            return null;
        }

        LocalDateTime start = stats.getStartedAt();
        LocalDateTime end = stats.getCompletedAt();
        if (start == null || end == null) {
            return null;
        }

        long totalSeconds = Duration.between(start, end).getSeconds();
        long hours   = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return "%dh %dm %ds".formatted(hours, minutes, seconds);
        } else if (minutes > 0) {
            return "%dm %ds".formatted(minutes, seconds);
        } else {
            return "%ds".formatted(seconds);
        }
    }
}

