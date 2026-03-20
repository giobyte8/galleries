package me.giobyte8.galleries.scanner.scanners.listeners;

import lombok.NonNull;
import me.giobyte8.galleries.persistence.models.ScanStats;
import me.giobyte8.galleries.persistence.models.ScanStatus;
import me.giobyte8.galleries.scanner.dto.ScanRequest;

import java.lang.ScopedValue;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory container for counters collected during a scan.
 * Acts as a request-scoped bridge/wrapper for ScanStats builder values.
 */
public class ScanStatsContext {

    private static final ScopedValue<ScanStatsContext> CURRENT = ScopedValue.newInstance();

    private final AtomicLong unchangedImages = new AtomicLong(0);
    private final AtomicLong newImages = new AtomicLong(0);
    private final AtomicLong updatedImages = new AtomicLong(0);
    private final AtomicLong notFoundImages = new AtomicLong(0);
    private final AtomicLong foundDirectories = new AtomicLong(0);

    private final ScanStats.ScanStatsBuilder statsBuilder;

    public ScanStatsContext(ScanRequest scanRequest) {
        Objects.requireNonNull(scanRequest, "scanRequest must not be null");
        this.statsBuilder = ScanStats.builder()
                .scanRequestId(scanRequest.id())
                .path(scanRequest.path());
    }

    public void incrementUnchangedImages() { unchangedImages.incrementAndGet(); }
    public void incrementNewImages() { newImages.incrementAndGet(); }
    public void incrementUpdatedImages() { updatedImages.incrementAndGet(); }
    public void incrementNotFoundImages() { notFoundImages.incrementAndGet(); }
    public void incrementFoundDirectories() { foundDirectories.incrementAndGet(); }

    public long getUnchangedImages() { return unchangedImages.get(); }
    public long getNewImages() { return newImages.get(); }
    public long getUpdatedImages() { return updatedImages.get(); }
    public long getNotFoundImages() { return notFoundImages.get(); }
    public long getFoundDirectories() { return foundDirectories.get(); }

    public void startedAt(LocalDateTime startedAt) {
        statsBuilder.startedAt(startedAt);
    }

    public void completedAt(LocalDateTime completedAt) {
        statsBuilder.completedAt(completedAt);
    }

    public void status(ScanStatus status) {
        statsBuilder.status(status);
    }

    public ScanStats buildStats() {
        return statsBuilder
                .newImages(getNewImages())
                .updatedImages(getUpdatedImages())
                .unchangedImages(getUnchangedImages())
                .notFoundImages(getNotFoundImages())
                .foundDirectories(getFoundDirectories())
                .build();
    }

    /**
     * Returns the ScanStatsContext bound to the current scope.
     * Calling this outside of a {@link #runWith} block is a programming error
     * and will throw {@link IllegalStateException}.
     */
    public static ScanStatsContext current() {
        if (!CURRENT.isBound()) {
            throw new IllegalStateException(
                    "No ScanStatsContext bound to current scope. " +
                    "Make sure to call ScanStatsContext.runWith(...) " +
                    "before invoking scan operations."
            );
        }
        return CURRENT.get();
    }

    public static void runWith(@NonNull ScanStatsContext context, Runnable runnable) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(runnable, "runnable must not be null");
        ScopedValue.where(CURRENT, context).run(runnable);
    }
}
