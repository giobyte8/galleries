package me.giobyte8.galleries.scanner.repository;

import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.Image;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class Neo4jDirectoryRepository implements DirectoryRepository {

    private final Driver driver;
    private final DirRowMapper rowMapper;

    public Neo4jDirectoryRepository(Driver driver, DirRowMapper rowMapper) {
        this.driver = driver;
        this.rowMapper = rowMapper;
    }

    @Override
    public int count() {
        try (Session session = driver.session()) {
            String countQuery = "MATCH (d:Directory) RETURN count(d) as count";

            return session.executeRead(tx -> {
                var res = tx.run(countQuery);
                if (res.hasNext()) {
                    return res.single().get("count").asInt();
                }

                return 0;
            });
        }
    }

    @Override
    public Directory findBy(String path) {
        try (Session session = driver.session()) {
            String findDir = "MATCH (d:Directory {path: $path}) RETURN d";

            return session.executeRead(tx -> {
                var res = tx.run(
                        findDir,
                        Values.parameters("path", path)
                );

                if (res.hasNext()) {
                    return rowMapper.from(res.single().get("d").asMap());
                }

                return null;
            });
        }
    }

    @Override
    public void save(Directory directory) {
        try (Session session = driver.session()) {
            String mergeDir = """
                MERGE (d:Directory { path: $path })
                ON CREATE
                    SET
                        d.recursive = $recursive,
                        d.status = $status
                ON MATCH
                    SET
                        d.recursive = $recursive,
                        d.status = $status;
            """;

            session.executeWriteWithoutResult(tCtx -> tCtx.run(
                    mergeDir,
                    rowMapper.asMap(directory)
            ));
        }
    }

    @Override
    public void save(Directory directory, Set<Image> images) {
        // TODO Put both operations in single transaction
        this.save(directory);
        this.addImages(directory.getPath(), images);
    }

    @Override
    public void addImage(String dirPath, Image image) {

    }

    @Override
    public void addImages(String dirPath, Set<Image> images) {

    }
}
