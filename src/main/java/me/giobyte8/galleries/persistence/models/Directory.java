package me.giobyte8.galleries.persistence.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Directory {
    private String path;
    private boolean recursive;

    @Builder.Default
    private DirStatus status = DirStatus.SCAN_PENDING;
}
