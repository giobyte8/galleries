package me.giobyte8.galleries.persistence.repositories;

import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.Image;
import me.giobyte8.galleries.persistence.models.ImageStatus;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class Neo4jImageRepository implements ImageRepository {

    private final Driver driver;
    private final ImgRowMapper rowMapper;

    public Neo4jImageRepository(Driver driver, ImgRowMapper rowMapper) {
        this.driver = driver;
        this.rowMapper = rowMapper;
    }

    @Override
    public long countBy(ImageStatus status) {
        try (Session session = driver.session()) {
            String countQuery = """
                MATCH (i:Image { status: $status })
                RETURN count(i) as count;""";

            Map<String, Object> params = new HashMap<>(1);
            params.put("status", status.toString());

            return session.executeRead(tx -> {
                var res = tx.run(countQuery, params);
                if (res.hasNext()) {
                    return res.single().get("count").asLong();
                }

                return 0L;
            });
        }
    }

    @Override
    public Image findByPath(String path) {
        try (Session session = driver.session()) {
            String query = "MATCH (i:Image { path: $path }) RETURN i";

            return session.executeRead(ctx -> {
                Image image = null;
                var res = ctx.run(query, Values.parameters(
                        "path",
                        path
                ));

                if (res.hasNext()) {
                    image = rowMapper.from(res.single().get("i").asMap());
                }

                return image;
            });
        }
    }

    public Image findByPathAndContentHash(String path, String hash) {
        try (Session session = driver.session()) {
            String query = """
                MATCH (i:Image {
                    path: $path,
                    contentHash: $hash
                })
                RETURN i""";

            return session.executeRead(ctx -> {
                Image image = null;
                var res = ctx.run(query, Values.parameters(
                        "hash",
                        hash,
                        "path",
                        path
                ));

                if (res.hasNext()) {
                    image = rowMapper.from(res.single().get("i").asMap());
                }

                return image;
            });
        }
    }

    @Override
    public Stream<Image> findBy(Directory parent) {

        // Currently restricting max depth to 5000 hops just as a safeguard
        // for performance. Real limit should be evaluated once in prod
        String findImagesQry = """
                MATCH (d:Directory { path: $dirPath })
                    -[:CONTAINS*..5000]
                    ->(i:Image)
                RETURN i""" ;

        Map<String, Object> params = new HashMap<>(1);
        params.put("dirPath", parent.getPath());

        var res = driver.executableQuery(findImagesQry)
                .withParameters(params)
                .execute();

        return res.records()
                .stream()
                .map(row -> rowMapper.from(row.get("i").asMap()));
    }

    @Override
    public Stream<Image> findBy(Directory parent, ImageStatus status) {
        return Stream.empty();
    }

    @Override
    public void save(Directory parent, Image image) {
        try (Session session = driver.session()) {
            String mergeImage = """
                    MATCH (d:Directory { path: $dirPath })
                    MERGE (d)-[:CONTAINS]->(i:Image { path: $path })
                    ON CREATE
                      SET
                        i.path = $path,
                        i.contentHash = $contentHash,
                        i.datetimeOriginal = $datetimeOriginal,
                        i.gpsLatitude = $gpsLatitude,
                        i.gpsLongitude = $gpsLongitude,
                        i.cameraMaker = $cameraMaker,
                        i.cameraModel = $cameraModel,
                        i.status = $status
                    ON MATCH
                      SET
                        i.path = $path,
                        i.contentHash = $contentHash,
                        i.datetimeOriginal = $datetimeOriginal,
                        i.gpsLatitude = $gpsLatitude,
                        i.gpsLongitude = $gpsLongitude,
                        i.cameraMaker = $cameraMaker,
                        i.cameraModel = $cameraModel,
                        i.status = $status
                    RETURN i;""";

            Map<String, Object> params = rowMapper.asMap(image);
            params.put("dirPath", parent.getPath());

            session.executeWrite(ctx -> {
                var res = ctx.run(mergeImage, params);
                if (!res.hasNext()) {
                    log.warn(
                            "Image wasn't saved. Verify parent dir exist: {}",
                            parent.getPath()
                    );
                }

                return res.single();
            });
        }
    }

    @Override
    public long update(Directory parent, ImageStatus status) {
        try (Session session = driver.session()) {
            String updateStatusQry = """
                    MATCH (d:Directory { path: $dirPath })-[:CONTAINS]->(i:Image)
                    WHERE i.status <> $status
                    SET i.status = $status
                    RETURN count(i) as updatedCount;""";

            Map<String, Object> params = new HashMap<>(2);
            params.put("dirPath", parent.getPath());
            params.put("status", status.toString());

            return session.executeWrite(ctx -> {
                var res = ctx.run(updateStatusQry, params);
                return res.single().get("updatedCount").asLong();
            });
        }
    }

    @Override
    public long delete(Directory parent, ImageStatus status) {
        try (Session session = driver.session()) {
            String deleteQry = """
                    MATCH (d:Directory { path: $dirPath })
                        -[:CONTAINS]
                        ->(i:Image { status: $status })
                    DETACH DELETE i
                    RETURN count(i) as deletedCount;""";

            Map<String, Object> params = new HashMap<>(2);
            params.put("dirPath", parent.getPath());
            params.put("status", status.toString());

            return session.executeWrite(ctx -> {
                var res = ctx.run(deleteQry, params);
                return res.single().get("deletedCount").asLong();
            });
        }
    }

    @Override
    public Set<String> deleteAndGetPaths(Directory parent, ImageStatus status) {
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

        var res = driver.executableQuery(deleteQry)
                .withParameters(params)
                .execute();

        return res.records()
                .stream()
                .map(record -> record.get("imgPath").asString())
                .collect(Collectors.toSet());
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

        var res = driver.executableQuery(deleteQry)
                .withParameters(params)
                .execute();

        return res.records()
                .stream()
                .map(record -> record.get("imgPath").asString())
                .collect(Collectors.toSet());
    }
}
