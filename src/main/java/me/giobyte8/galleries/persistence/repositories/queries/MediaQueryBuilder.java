package me.giobyte8.galleries.persistence.repositories.queries;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class MediaQueryBuilder {
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


    private boolean directChildrenOnly = false;
    private boolean includeOriginalVersions = false;
    private Sort sort = Sort.unsorted();

    public MediaQuery build() {
        var mediaQuery = mediaQuery();
        log.debug("Media query: {}", mediaQuery);

        var mediaCountQuery = mediaCountQuery();
        log.debug("Media count query: {}", mediaCountQuery);

        return new MediaQuery(mediaQuery, mediaCountQuery);
    }

    public MediaQueryBuilder directChildrenOnly(boolean directChildrenOnly) {
        this.directChildrenOnly = directChildrenOnly;
        return this;
    }

    @SuppressWarnings("unused")
    public MediaQueryBuilder includeOriginalVersions() {
        this.includeOriginalVersions = true;
        return this;
    }

    public MediaQueryBuilder sort(Sort sort) {
        this.sort = sort;
        return this;
    }

    private String mediaQuery() {
        var qryTemplate = """
            MATCH (d:Directory { id: $parentId })
            CALL (d) {
                %s
            
                UNION ALL
                %s
            }
            RETURN path, version, fileSize,
                   captureDateTime, captureInstant,
                   rawCaptureDateTime, lastModified,
                   gpsLatitude, gpsLongitude,
                   cameraMaker, cameraModel,
                   status, mediaType
            ORDER BY %s
            SKIP $skip LIMIT $limit
            """;

        return qryTemplate.formatted(
                padLeft(4, matchImages()),
                padLeft(4, matchVideos()),
                orderBy()
        );
    }

    private String mediaCountQuery() {
        var relationship = directChildrenOnly
                ? CONTAINS
                : CONTAINS_RECURSIVE;

        var queryTemplate = """
            MATCH (d:Directory { id: $parentId })
            CALL (d) {
                MATCH (d)%s(i:Image)
                %s
                RETURN i.path AS path
            
                UNION ALL
                MATCH (d)%s(v:Video)
                %s
                RETURN v.path AS path
            }
            RETURN count(path) AS total
            """;

        return queryTemplate.formatted(
                relationship,
                padLeft(4, filterImages()),

                relationship,
                padLeft(4, filterVideos())
        );
    }

    private String matchImages() {
        var relationship = directChildrenOnly
                ? CONTAINS
                : CONTAINS_RECURSIVE;

        var matchTemplate = """
            MATCH (d)%s(i:Image)
            %s
            RETURN
                i.path               AS path,
                i.version            AS version,
                i.fileSize           AS fileSize,
                i.captureDateTime    AS captureDateTime,
                i.captureInstant     AS captureInstant,
                i.rawCaptureDateTime AS rawCaptureDateTime,
                i.lastModified       AS lastModified,
                i.gpsLatitude        AS gpsLatitude,
                i.gpsLongitude       AS gpsLongitude,
                i.cameraMaker        AS cameraMaker,
                i.cameraModel        AS cameraModel,
                i.status             AS status,
                'IMAGE'              AS mediaType
            """;

        return matchTemplate.formatted(
                relationship,
                filterImages()
        );
    }

    private String matchVideos() {
        var relationship = directChildrenOnly
                ? CONTAINS
                : CONTAINS_RECURSIVE;

        var matchTemplate = """
            MATCH (d)%s(v:Video)
            %s
            RETURN
                v.path               AS path,
                v.version            AS version,
                v.fileSize           AS fileSize,
                v.captureDateTime    AS captureDateTime,
                v.captureInstant     AS captureInstant,
                v.rawCaptureDateTime AS rawCaptureDateTime,
                v.lastModified       AS lastModified,
                v.gpsLatitude        AS gpsLatitude,
                v.gpsLongitude       AS gpsLongitude,
                v.cameraMaker        AS cameraMaker,
                v.cameraModel        AS cameraModel,
                v.status             AS status,
                'VIDEO'              AS mediaType
            """;

        return matchTemplate.formatted(
                relationship,
                filterVideos()
        );
    }

    private String filterImages() {
        if (includeOriginalVersions) return "";

        return """
            WHERE NOT EXISTS {
                MATCH (:Image)-[:EDITS]->(i)
            }""";
    }

    private String filterVideos() {
        if (includeOriginalVersions) return "";

        return """
            WHERE NOT EXISTS {
                MATCH (:Video)-[:EDITS]->(v)
            }""";
    }

    private String orderBy() {
        List<String> clauses = StreamSupport
                .stream(sort.spliterator(), false)
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

    /// Pads every line (except the first) in a given string by
    /// adding the required number of spaces at the beginning
    @SuppressWarnings("SameParameterValue")
    private String padLeft(int padLength, String str) {
        if (str.isBlank()) return str;

        // First line doesn't need padding
        var firstLine = str.lines().findFirst().orElse("");

        var paddedContent =  str.lines()
                .skip(1)
                .map(line -> "%s%s".formatted(" ".repeat(padLength), line))
                .collect(Collectors.joining("\n"));

        return firstLine + "\n" + paddedContent;
    }
}
