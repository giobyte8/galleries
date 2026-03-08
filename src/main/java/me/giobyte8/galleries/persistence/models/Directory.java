package me.giobyte8.galleries.persistence.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Data
@Builder
@Node
public class Directory {

    @Id
    private String path;

    @Version
    private Long version;

    private boolean recursive;

    @Builder.Default
    private DirStatus status = DirStatus.SCAN_PENDING;
}
