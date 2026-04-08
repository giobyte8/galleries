package me.giobyte8.galleries.persistence.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tracks statistics about a directory scan operation.
 * Records what was discovered/changed during the scan.
 */
@Data
@Builder
@Node
public class ScanStats {

    @Id
    @GeneratedValue
    private UUID id;

    /** The scan request ID that initiated this scan */
    private UUID scanRequestId;

    /** The directory path that was scanned */
    private String path;

    /** When the scan started */
    private LocalDateTime startedAt;

    /** When the scan completed (null if still in progress) */
    private LocalDateTime completedAt;

    /** Number of images that were already in the index and unchanged */
    private long unchangedImages;

    /** Number of newly discovered images */
    private long newImages;

    /** Number of previously indexed images that were updated */
    private long updatedImages;

    /** Number of previously indexed images that were not found */
    private long notFoundImages;

    /** Number of videos that were already in the index and unchanged */
    private long unchangedVideos;

    /** Number of newly discovered videos */
    private long newVideos;

    /** Number of previously indexed videos that were updated */
    private long updatedVideos;

    /** Number of previously indexed videos that were not found */
    private long notFoundVideos;

    /** Number of directories discovered during scan */
    private long foundDirectories;

    @Version
    private Long version;

    @Builder.Default
    private ScanStatus status = ScanStatus.IN_PROGRESS;
}

