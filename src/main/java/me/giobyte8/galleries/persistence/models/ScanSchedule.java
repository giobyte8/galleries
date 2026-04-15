package me.giobyte8.galleries.persistence.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Node
public class ScanSchedule {

    @Id
    @GeneratedValue
    private UUID id;

    private String schedule;

    private String tzOffset;

    private boolean enabled;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @Relationship(type = "SCANS", direction = Relationship.Direction.OUTGOING)
    private Directory directory;
}









