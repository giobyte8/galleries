package me.giobyte8.galleries.scanner.repository;

import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.Image;
import me.giobyte8.galleries.scanner.model.ImageStatus;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Service;

import java.util.Map;
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
        return 0;
    }

    @Override
    public Image findBy(String path) {
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

    @Override
    public Stream<Image> findBy(Directory parent) {
        return Stream.empty();
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

                return res;
            });
        }
    }
}
