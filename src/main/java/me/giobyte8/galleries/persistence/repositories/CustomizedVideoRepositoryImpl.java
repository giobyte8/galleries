package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.mappers.VideoRowMapper;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.MediaFileStatus;
import me.giobyte8.galleries.persistence.models.Video;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class CustomizedVideoRepositoryImpl implements CustomizedVideoRepository {

    private final Neo4jClient neo4jClient;
    private final VideoRowMapper rowMapper;

    public CustomizedVideoRepositoryImpl(
            Neo4jClient neo4jClient,
            VideoRowMapper rowMapper
    ) {
        this.neo4jClient = neo4jClient;
        this.rowMapper = rowMapper;
    }

    @Override
    public Optional<Video> saveAsChild(Directory parent, Video video) {
        String mergeVideo = """
                MATCH (d:Directory { path: $dirPath })
                MERGE (d)-[:CONTAINS]->(v:Video { path: $path })
                ON CREATE
                  SET
                    v.path = $path,
                    v.contentHash = $contentHash,
                    v.captureDateTime = $captureDateTime,
                    v.captureInstant = $captureInstant,
                    v.rawCaptureDateTime = $rawCaptureDateTime,
                    v.gpsLatitude = $gpsLatitude,
                    v.gpsLongitude = $gpsLongitude,
                    v.cameraMaker = $cameraMaker,
                    v.cameraModel = $cameraModel,
                    v.status = $status,
                    v.version = 0
                ON MATCH
                  SET
                    v.path = $path,
                    v.contentHash = $contentHash,
                    v.captureDateTime = $captureDateTime,
                    v.captureInstant = $captureInstant,
                    v.rawCaptureDateTime = $rawCaptureDateTime,
                    v.gpsLatitude = $gpsLatitude,
                    v.gpsLongitude = $gpsLongitude,
                    v.cameraMaker = $cameraMaker,
                    v.cameraModel = $cameraModel,
                    v.status = $status,
                    v.version = coalesce(v.version, 0) + 1
                RETURN v;""";

        Map<String, Object> params = rowMapper.asMap(video);
        params.put("dirPath", parent.getPath());

        var upsertedVideoOpt = neo4jClient.query(mergeVideo)
                .bindAll(params)
                .fetchAs(Video.class)
                .mappedBy((_, record) ->
                        rowMapper.from(record.get("v").asMap())
                )
                .one();

        upsertedVideoOpt.ifPresent(upsertedVideo ->
                video.setVersion(upsertedVideo.getVersion())
        );

        return upsertedVideoOpt;
    }

    @Override
    public long updateStatusByParent(Directory parent, MediaFileStatus status) {
        String updateStatusQry = """
                MATCH (d:Directory { path: $dirPath })-[:CONTAINS]->(v:Video)
                WHERE v.status <> $status
                SET v.status = $status
                RETURN count(v) as updatedCount;""";

        Map<String, Object> params = new HashMap<>(2);
        params.put("dirPath", parent.getPath());
        params.put("status", status.toString());

        return neo4jClient.query(updateStatusQry)
                .bindAll(params)
                .fetchAs(Long.class)
                .mappedBy((_, record) ->
                        record.get("updatedCount").asLong()
                )
                .one()
                .orElse(0L);
    }

    @Override
    public long deleteByParentAndStatus(Directory parent, MediaFileStatus status) {
        String deleteQry = """
                MATCH (d:Directory { path: $dirPath })
                    -[:CONTAINS]
                    ->(v:Video { status: $status })
                DETACH DELETE v
                RETURN count(v) as deletedCount;""";

        Map<String, Object> params = new HashMap<>(2);
        params.put("dirPath", parent.getPath());
        params.put("status", status.toString());

        return neo4jClient.query(deleteQry)
                .bindAll(params)
                .fetchAs(Long.class)
                .mappedBy((_, record) ->
                        record.get("deletedCount").asLong()
                )
                .one()
                .orElse(0L);
    }

    @Override
    public Set<String> deleteAndGetPaths(Directory parent, MediaFileStatus status) {
        String deleteQry = """
                MATCH (d:Directory { path: $dirPath })
                    -[:CONTAINS]
                    ->(v:Video { status: $status })
                WITH v, v.path AS videoPath
                DETACH DELETE v
                RETURN videoPath""";

        Map<String, Object> params = new HashMap<>(2);
        params.put("dirPath", parent.getPath());
        params.put("status", status.toString());

        var results = neo4jClient.query(deleteQry)
                .bindAll(params)
                .fetchAs(String.class)
                .mappedBy((_, record) ->
                        record.get("videoPath").asString()
                )
                .all();
        return new HashSet<>(results);
    }

    @Override
    public Set<String> multilevelDeleteAndGetPaths(Directory parent) {
        String deleteQry = """
                MATCH (d:Directory { path: $dirPath })
                    -[:CONTAINS*..5000]
                    ->(v:Video)
                WITH v, v.path as videoPath
                DETACH DELETE v
                RETURN videoPath""";

        Map<String, Object> params = new HashMap<>();
        params.put("dirPath", parent.getPath());

        var results = neo4jClient.query(deleteQry)
                .bindAll(params)
                .fetchAs(String.class)
                .mappedBy((_, record) ->
                        record.get("videoPath").asString()
                )
                .all();
        return new HashSet<>(results);
    }
}

