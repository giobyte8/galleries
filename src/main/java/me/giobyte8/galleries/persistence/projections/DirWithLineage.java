package me.giobyte8.galleries.persistence.projections;

import lombok.Builder;
import me.giobyte8.galleries.persistence.models.Directory;

import java.util.List;

@Builder
public record DirWithLineage(
        Directory directory,
        List<Directory> lineage
) { }
