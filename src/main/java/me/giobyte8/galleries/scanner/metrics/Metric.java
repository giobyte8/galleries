package me.giobyte8.galleries.scanner.metrics;

import lombok.Getter;

@Getter
public enum Metric {

    SCAN_DIR_STARTED("scan.dir.started"),
    SCAN_DIR_COMPLETED("scan.dir.completed"),
    SCAN_DIR_FAILED("scan.dir.failed"),
    SCAN_DIR_FOUND("scan.dir.found"),

    SCAN_IMG_FOUND_NEW("scan.image.found.new"),
    SCAN_IMG_FOUND_UPDATED("scan.image.found.updated"),
    SCAN_IMG_FOUND_UNCHANGED("scan.image.found.unchanged"),
    SCAN_IMG_NOT_FOUND("scan.image.not.found"),

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
