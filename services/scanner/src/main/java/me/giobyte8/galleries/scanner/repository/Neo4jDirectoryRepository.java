package me.giobyte8.galleries.scanner.repository;

import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.scanner.model.Directory;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
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
    public void save(Directory parent, Directory directory) {
        try (Session session = driver.session()) {
            String mergeDir = """
                    MATCH (parent:Directory { path: $parentPath })
                    MERGE (parent)-[:CONTAINS]->(d:Directory { path: $path })
                    ON CREATE
                      SET
                        d.recursive = $recursive,
                        d.status = $status
                    ON MATCH
                      SET
                        d.recursive = $recursive,
                        d.status = $status
                    RETURN d;""";

            Map<String, Object> params = rowMapper.asMap(directory);
            params.put("parentPath", parent.getPath());

            session.executeWrite(ctx -> {
                var res = ctx.run(mergeDir, params);
                if (!res.hasNext()) {
                    log.warn(
                            "Directory wasn't saved. Verify parent dir exist: {}",
                            parent.getPath()
                    );
                }

                return res.single();
            });
        }
    }
}
