package me.giobyte8.galleries.scanner.metrics;

import lombok.Getter;

@Getter
public enum MetricTag {

    MEDIA_TYPE("media_type"),
    FOUND_TYPE("found_type");

    private final String name;

    MetricTag(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

