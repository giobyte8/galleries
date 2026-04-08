package me.giobyte8.galleries.scanner.metrics;

import lombok.Getter;

@Getter
public enum Metric {

    SCAN_STARTED("scan.started"),
    SCAN_COMPLETED("scan.completed"),

    SCAN_DIR_STARTED("scan.dir.started"),
    SCAN_DIR_COMPLETED("scan.dir.completed"),
    SCAN_DIR_FAILED("scan.dir.failed"),
    SCAN_DIR_FOUND("scan.dir.found"),

    SCAN_IMG_FOUND_NEW("scan.image.found.new"),
    SCAN_IMG_FOUND_UPDATED("scan.image.found.updated"),
    SCAN_IMG_FOUND_UNCHANGED("scan.image.found.unchanged"),
    SCAN_IMG_NOT_FOUND("scan.image.not.found"),

    SCAN_VIDEO_FOUND_NEW("scan.video.found.new"),
    SCAN_VIDEO_FOUND_UPDATED("scan.video.found.updated"),
    SCAN_VIDEO_FOUND_UNCHANGED("scan.video.found.unchanged"),
    SCAN_VIDEO_NOT_FOUND("scan.video.not.found"),

    THUMBS_REQUESTED_GEN("thumbs.requested.generate"),
    THUMBS_REQUESTED_REF("thumbs.requested.refresh"),
    THUMBS_REQUESTED_DEL("thumbs.requested.delete");


    private final String name;

    Metric(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
