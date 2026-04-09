package me.giobyte8.galleries.scanner.metrics;

import lombok.Getter;

@Getter
public enum Metric {

    @Deprecated SCAN_STARTED("scan.started"),
    @Deprecated SCAN_COMPLETED("scan.completed"),
    SCAN_REQUEST(MetricStr.SCAN_REQUEST),

    SCAN_DIR_STARTED("gl.scan.dir.started"),
    SCAN_DIR_COMPLETED("gl.scan.dir.completed"),
    SCAN_DIR_FAILED("gl.scan.dir.failed"),
    SCAN_DIR_FOUND("gl.scan.dir.found"),

    @Deprecated SCAN_IMG_FOUND_NEW("scan.image.found.new"),
    @Deprecated SCAN_IMG_FOUND_UPDATED("scan.image.found.updated"),
    @Deprecated SCAN_IMG_FOUND_UNCHANGED("scan.image.found.unchanged"),
    @Deprecated SCAN_IMG_NOT_FOUND("scan.image.not.found"),

    @Deprecated SCAN_VIDEO_FOUND_NEW("scan.video.found.new"),
    @Deprecated SCAN_VIDEO_FOUND_UPDATED("scan.video.found.updated"),
    @Deprecated SCAN_VIDEO_FOUND_UNCHANGED("scan.video.found.unchanged"),
    @Deprecated SCAN_VIDEO_NOT_FOUND("scan.video.not.found"),

    /// Timer (with an implicit counter) to track each found file and the
    /// time to process it. Consider using following tags:
    /// - media_type: video|image
    /// - found_type: new|updated|unchanged
    SCAN_MEDIA_FOUND("gl.scan.media.found"),

    /// Tracks each metadata extraction for each found file.
    /// Consider following tags:
    /// - media_type: video|image
    SCAN_META_EXTRACTION("gl.scan.media.meta-extraction"),

    /// Tracks every time that a found file is checked against its
    /// previous version in database to detect if it has changed.
    /// Consider following tags:
    /// - media_type: video|image
    SCAN_MEDIA_CHANGE_DETECTION("gl.scan.media.change-detection"),

    THUMBS_REQUESTED_GEN("gl.thumbs.req.generate"),
    THUMBS_REQUESTED_REF("gl.thumbs.req.refresh"),
    THUMBS_REQUESTED_DEL("gl.thumbs.req.delete");


    private final String name;

    Metric(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
