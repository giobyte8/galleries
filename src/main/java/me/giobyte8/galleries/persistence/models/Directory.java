package me.giobyte8.galleries.persistence.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.UUID;

@Data
@Builder
@Node
public class Directory {

    @Id
    @GeneratedValue
    private UUID id;
    private String path;
    private boolean recursive;

    @Version
    private Long version;

    @Builder.Default
    private DirStatus status = DirStatus.SCAN_PENDING;
}
