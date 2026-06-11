package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.dto.MediaItemDto;
import me.giobyte8.galleries.persistence.mappers.MediaRowMapper;
import me.giobyte8.galleries.persistence.repositories.queries.MediaQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class MediaRepositoryImpl implements MediaRepository {
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
        return query(parentId, pageable, false);
    }

    @Override
    public Page<MediaItemDto> findAllMediaByParentIdRecursively(
            UUID parentId,
            Pageable pageable
    ) {
        return query(parentId, pageable, true);
    }

    private Page<MediaItemDto> query(
            UUID parentId,
            Pageable pageable,
            boolean recursive
    ) {
        var cypherQueries = MediaQuery.builder()
//                .includeOriginalVersions()
                .directChildrenOnly(!recursive)
                .sort(pageable.getSort())
                .build();

        Map<String, Object> params = Map.of(
                "parentId", parentId.toString(),
                "skip", pageable.getOffset(),
                "limit", pageable.getPageSize()
        );

        List<MediaItemDto> content = new ArrayList<>(neo4jClient
                .query(cypherQueries.getQuery())
                .bindAll(params)
                .fetchAs(MediaItemDto.class)
                .mappedBy((_, r) -> rowMapper.from(r))
                .all()
        );

        long total = neo4jClient
                .query(cypherQueries.getCountQuery())
                .bindAll(Map.of("parentId", parentId.toString()))
                .fetchAs(Long.class)
                .mappedBy((_, r) -> r.get("total").asLong())
                .one()
                .orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }
}
