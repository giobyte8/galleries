package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.mappers.ImgRowMapper;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.Image;
import me.giobyte8.galleries.persistence.models.MediaFileStatus;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CustomizedImageRepositoryImpl implements CustomizedImageRepository {

    private final Neo4jClient neo4jClient;
    private final ImgRowMapper rowMapper;

    public CustomizedImageRepositoryImpl(
            Neo4jClient neo4jClient,
            ImgRowMapper rowMapper
    ) {
        this.neo4jClient = neo4jClient;
        this.rowMapper = rowMapper;
    }

    @Override
    public Optional<Image> saveAsChild(Directory parent, Image image) {
        String mergeImage = """
                MATCH (d:Directory { path: $dirPath })
                MERGE (d)-[:CONTAINS]->(i:Image { path: $path })
                ON CREATE
                  SET
                    i.path = $path,
                    i.fileSize = $fileSize,
                    i.lastModified = $lastModified,
                    i.captureDateTime = $captureDateTime,
                    i.captureInstant = $captureInstant,
                    i.rawCaptureDateTime = $rawCaptureDateTime,
                    i.gpsLatitude = $gpsLatitude,
                    i.gpsLongitude = $gpsLongitude,
                    i.cameraMaker = $cameraMaker,
                    i.cameraModel = $cameraModel,
                    i.status = $status,
                    i.format = $format,
                    i.version = 0
                ON MATCH
                  SET
                    i.path = $path,
                    i.fileSize = $fileSize,
                    i.lastModified = $lastModified,
                    i.captureDateTime = $captureDateTime,
                    i.captureInstant = $captureInstant,
                    i.rawCaptureDateTime = $rawCaptureDateTime,
                    i.gpsLatitude = $gpsLatitude,
                    i.gpsLongitude = $gpsLongitude,
                    i.cameraMaker = $cameraMaker,
                    i.cameraModel = $cameraModel,
                    i.status = $status,
                    i.format = $format,
                    i.version = coalesce(i.version, 0) + 1
                RETURN i;""";

        Map<String, Object> params = rowMapper.asMap(image);
        params.put("dirPath", parent.getPath());

        var upsertedImgOpt = neo4jClient.query(mergeImage)
                .bindAll(params)
                .fetchAs(Image.class)
                .mappedBy((_, record) ->
                        rowMapper.from(record.get("i").asMap())
                )
                .one();

        upsertedImgOpt.ifPresent(upsertedImg ->
                image.setVersion(upsertedImg.getVersion())
        );

        return upsertedImgOpt;
    }

    @Override
    public long updateStatusByParent(Directory parent, MediaFileStatus status) {
        String updateStatusQry = """
                MATCH (d:Directory { path: $dirPath })-[:CONTAINS]->(i:Image)
                WHERE i.status <> $status
                SET i.status = $status
                RETURN count(i) as updatedCount;""";

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
                    ->(i:Image { status: $status })
                DETACH DELETE i
                RETURN count(i) as deletedCount;""";

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
                    ->(i:Image { status: $status })
                WITH i, i.path AS imgPath
                DETACH DELETE i
                RETURN imgPath""";

        Map<String, Object> params = new HashMap<>(2);
        params.put("dirPath", parent.getPath());
        params.put("status", status.toString());

        var results = neo4jClient.query(deleteQry)
                .bindAll(params)
                .fetchAs(String.class)
                .mappedBy((_, record) ->
                        record.get("imgPath").asString()
                )
                .all();
        return new HashSet<>(results);
    }

    @Override
    public Set<String> multilevelDeleteAndGetPaths(Directory parent) {

        // Currently restricting max depth to 5000 hops just as a safeguard
        // for performance. Real limit should be evaluated once in prod
        String deleteQry = """
                MATCH (d:Directory { path: $dirPath })
                    -[:CONTAINS*..5000]
                    ->(i:Image)
                WITH i, i.path as imgPath
                DETACH DELETE i
                RETURN imgPath""";

        Map<String, Object> params = new HashMap<>();
        params.put("dirPath", parent.getPath());

        var results = neo4jClient.query(deleteQry)
                .bindAll(params)
                .fetchAs(String.class)
                .mappedBy((_, record) ->
                        record.get("imgPath").asString()
                )
                .all();
        return new HashSet<>(results);
    }

    @Override
    public List<String> findUnlinkedEditedPathsByParent(
            Directory parent,
            String afterPath,
            int limit
    ) {
        String findEditedQry = """
                MATCH (d:Directory { path: $dirPath })
                    -[:CONTAINS]
                    ->(i:Image)
                WHERE
                    NOT (i)-[:EDITS]->(:Image)
                    AND toLower(i.path) CONTAINS '_edit.'
                    AND ($afterPath IS NULL OR i.path > $afterPath)
                RETURN i.path AS path
                ORDER BY path ASC
                LIMIT $limit""";

        Map<String, Object> params = new HashMap<>(3);
        params.put("dirPath", parent.getPath());
        params.put("afterPath", afterPath);
        params.put("limit", limit);

        return new ArrayList<>(neo4jClient.query(findEditedQry)
                .bindAll(params)
                .fetchAs(String.class)
                .mappedBy((_, record) ->
                        record.get("path").asString()
                )
                .all());
    }

    @Override
    public boolean linkEditedToOriginal(
            Directory parent,
            String editedPath,
            String originalPath
    ) {
        String linkQry = """
                MATCH (d:Directory { path: $dirPath })
                    -[:CONTAINS]
                    ->(edited:Image { path: $editedPath })
                WHERE NOT (edited)-[:EDITS]->(:Image)
                MATCH (d)-[:CONTAINS]->(original:Image { path: $originalPath })
                MERGE (edited)-[r:EDITS]->(original)
                RETURN count(r) AS linkedCount""";

        Map<String, Object> params = new HashMap<>(3);
        params.put("dirPath", parent.getPath());
        params.put("editedPath", editedPath);
        params.put("originalPath", originalPath);

        long linkedCount = neo4jClient.query(linkQry)
                .bindAll(params)
                .fetchAs(Long.class)
                .mappedBy((_, record) ->
                        record.get("linkedCount").asLong()
                )
                .one()
                .orElse(0L);

        return linkedCount > 0;
    }
}
