package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.dto.MediaItemDto;
import me.giobyte8.galleries.persistence.mappers.MediaRowMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.StreamSupport;

@Repository
public class MediaRepositoryImpl implements MediaRepository {

    private static final String CONTAINS =
            "-[:CONTAINS]->";
    private static final String CONTAINS_RECURSIVE =
            "-[:CONTAINS*..5000]->";

    private static final String DEFAULT_ORDER_BY =
            "captureInstant DESC, lastModified DESC, path ASC";

    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "path", "captureInstant", "lastModified",
            "fileSize", "status", "mediaType"
    );

    private final Neo4jClient neo4jClient;
    private final MediaRowMapper rowMapper;

    public MediaRepositoryImpl(
            Neo4jClient neo4jClient,
            MediaRowMapper rowMapper
    ) {
        this.neo4jClient = neo4jClient;
        this.rowMapper = rowMapper;
    }

    @Override
    public Page<MediaItemDto> findAllMediaByParentId(
            UUID parentId,
            Pageable pageable
    ) {
        return query(parentId, pageable, CONTAINS);
    }

    @Override
    public Page<MediaItemDto> findAllMediaByParentIdRecursively(
            UUID parentId,
            Pageable pageable
    ) {
        return query(parentId, pageable, CONTAINS_RECURSIVE);
    }

    private Page<MediaItemDto> query(
            UUID parentId,
            Pageable pageable,
            String rel
    ) {
        Map<String, Object> params = Map.of(
                "parentId", parentId.toString(),
                "skip", pageable.getOffset(),
                "limit", pageable.getPageSize()
        );

        List<MediaItemDto> content = new ArrayList<>(
                neo4jClient
                        .query(mediaQuery(rel, resolveOrderBy(pageable)))
                        .bindAll(params)
                        .fetchAs(MediaItemDto.class)
                        .mappedBy((_, r) -> rowMapper.from(r))
                        .all()
        );

        long total = neo4jClient
                .query(countQuery(rel))
                .bindAll(Map.of("parentId", parentId.toString()))
                .fetchAs(Long.class)
                .mappedBy((_, r) -> r.get("total").asLong())
                .one()
                .orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    private static String mediaQuery(String rel, String orderBy) {
        return """
                MATCH (d:Directory { id: $parentId })
                CALL {
                    WITH d
                    MATCH (d)%s(i:Image)
                    RETURN i.path            AS path,
                           i.version         AS version,
                           i.fileSize        AS fileSize,
                           i.captureDateTime AS captureDateTime,
                           i.captureInstant  AS captureInstant,
                           i.rawCaptureDateTime AS rawCaptureDateTime,
                           i.lastModified    AS lastModified,
                           i.gpsLatitude     AS gpsLatitude,
                           i.gpsLongitude    AS gpsLongitude,
                           i.cameraMaker     AS cameraMaker,
                           i.cameraModel     AS cameraModel,
                           i.status          AS status,
                           'IMAGE'           AS mediaType
                    UNION ALL
                    WITH d
                    MATCH (d)%s(v:Video)
                    RETURN v.path            AS path,
                           v.version         AS version,
                           v.fileSize        AS fileSize,
                           v.captureDateTime AS captureDateTime,
                           v.captureInstant  AS captureInstant,
                           v.rawCaptureDateTime AS rawCaptureDateTime,
                           v.lastModified    AS lastModified,
                           v.gpsLatitude     AS gpsLatitude,
                           v.gpsLongitude    AS gpsLongitude,
                           v.cameraMaker     AS cameraMaker,
                           v.cameraModel     AS cameraModel,
                           v.status          AS status,
                           'VIDEO'           AS mediaType
                }
                RETURN path, version, fileSize,
                       captureDateTime, captureInstant,
                       rawCaptureDateTime, lastModified,
                       gpsLatitude, gpsLongitude,
                       cameraMaker, cameraModel,
                       status, mediaType
                ORDER BY %s
                SKIP $skip LIMIT $limit
                """.formatted(rel, rel, orderBy);
    }

    private static String countQuery(String rel) {
        return """
                MATCH (d:Directory { id: $parentId })
                CALL {
                    WITH d
                    MATCH (d)%s(i:Image)
                    RETURN i.path AS path
                    UNION ALL
                    WITH d
                    MATCH (d)%s(v:Video)
                    RETURN v.path AS path
                }
                RETURN count(path) AS total
                """.formatted(rel, rel);
    }

    private String resolveOrderBy(Pageable pageable) {
        List<String> clauses = StreamSupport
                .stream(pageable.getSort().spliterator(), false)
                .filter(o -> SORTABLE_FIELDS.contains(o.getProperty()))
                .map(o -> o.getProperty() + " " + o.getDirection().name())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (clauses.isEmpty()) {
            return DEFAULT_ORDER_BY;
        }

        // Append tie-breakers for stable pagination across pages
        if (clauses.stream().noneMatch(c -> c.startsWith("path "))) {
            clauses.add("path ASC");
        }
        if (clauses.stream().noneMatch(c -> c.startsWith("mediaType "))) {
            clauses.add("mediaType ASC");
        }

        return String.join(", ", clauses);
    }
}
