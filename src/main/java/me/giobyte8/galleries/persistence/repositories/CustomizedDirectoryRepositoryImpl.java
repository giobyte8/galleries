package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.mappers.DirRowMapper;
import me.giobyte8.galleries.persistence.models.DirStatus;
import me.giobyte8.galleries.persistence.models.Directory;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CustomizedDirectoryRepositoryImpl implements CustomizedDirectoryRepository {

    private final Neo4jClient neo4jClient;
    private final DirRowMapper rowMapper;

    public CustomizedDirectoryRepositoryImpl(
            Neo4jClient neo4jClient,
            DirRowMapper rowMapper
    ) {
        this.neo4jClient = neo4jClient;
        this.rowMapper = rowMapper;
    }

    @Override
    public Set<Directory> findByParentPathAndStatus(
            Directory parent,
            DirStatus status
    ) {
        String query = """
                MATCH (p:Directory {path: $parentPath})
                    -[:CONTAINS]
                    ->(dir:Directory { status: $status })
                RETURN dir;""";

        Map<String, Object> params = new HashMap<>(2);
        params.put("parentPath", parent.getPath());
        params.put("status", status.toString());

        var results = neo4jClient.query(query)
                .bindAll(params)
                .fetchAs(Directory.class)
                .mappedBy((_, record) ->
                        rowMapper.from(record.get("dir").asMap())
                )
                .all();
        return new HashSet<>(results);
    }

    @Override
    public Optional<Directory> saveAsChild(
            Directory parent,
            Directory directory
    ) {
        String mergeDir = """
                MATCH (parent:Directory { path: $parentPath })
                MERGE (parent)-[:CONTAINS]->(d:Directory { path: $path })
                ON CREATE
                  SET
                    d.id        = $id,
                    d.recursive = $recursive,
                    d.version   = 0,
                    d.status    = $status
                ON MATCH
                  SET
                    d.recursive = $recursive,
                    d.version   = coalesce(d.version, 0) + 1,
                    d.status    = $status
                RETURN d;""";

        Map<String, Object> params = new HashMap<>();
        params.put("parentPath", parent.getPath());
        params.put("path", directory.getPath());

        // Generate new UUID when Spring Data Neo4j hasn't assigned an ID yet
        params.put("id", Objects.isNull(directory.getId())
                ? UUID.randomUUID().toString()
                : directory.getId().toString()
        );
        params.put("recursive", directory.isRecursive());
        params.put("status", directory.getStatus().toString());

        var upsertedDirOpt = neo4jClient.query(mergeDir)
                .bindAll(params)
                .fetchAs(Directory.class)
                .mappedBy((_, record) ->
                        rowMapper.from(record.get("d").asMap())
                )
                .one();

        upsertedDirOpt.ifPresent(upsertedDir -> {
            directory.setId(upsertedDir.getId());
            directory.setVersion(upsertedDir.getVersion());
        });

        return upsertedDirOpt;
    }

    @Override
    public long updateStatusByParent(Directory parent, DirStatus status) {
        String updateQry = """
                MATCH (parent:Directory { path: $parentPath })
                  -[:CONTAINS]
                  ->(d:Directory)
                WHERE d.status <> $status
                SET d.status = $status
                RETURN count(d) as updatedCount""";

        Map<String, Object> params = new HashMap<>();
        params.put("parentPath", parent.getPath());
        params.put("status", status.toString());

        return neo4jClient.query(updateQry)
                .bindAll(params)
                .fetchAs(Long.class)
                .mappedBy((_, record) ->
                        record.get("updatedCount").asLong()
                )
                .one()
                .orElse(0L);
    }

    @Override
    public long deleteWithDescendants(Directory parent) {
        var deleteQry = """
                MATCH (dir:Directory { path: $parentPath })
                    -[:CONTAINS*..10000]
                    ->(sub:Directory)
                DETACH DELETE sub
                DETACH DELETE dir
                WITH count(sub) as deletedSubCount
                RETURN (deletedSubCount + 1) AS deletedCount;""";

        Map<String, Object> params = new HashMap<>();
        params.put("parentPath", parent.getPath());

        return neo4jClient.query(deleteQry)
                .bindAll(params)
                .fetchAs(Long.class)
                .mappedBy((_, record) ->
                        record.get("deletedCount").asLong()
                )
                .one()
                .orElse(0L);
    }
}
