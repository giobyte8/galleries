package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.models.Image;
import me.giobyte8.galleries.persistence.models.ImageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ImageRepository extends
        CrudRepository<Image, String>,
        PagingAndSortingRepository<Image, String>,
        CustomizedImageRepository {

    long countByStatus(ImageStatus status);

    Optional<Image> findByPath(String path);

    Image findByPathAndContentHash(String path, String contentHash);

    @Query(
            value = """
                    MATCH (d:Directory { id: $parentId })
                        -[:CONTAINS]
                        ->(i:Image)
                    RETURN i
                    :#{orderBy(#pageable)}
                    SKIP $skip
                    LIMIT $limit
                    """,
            countQuery = """
                    MATCH (d:Directory { id: $parentId })
                        -[:CONTAINS]
                        ->(i:Image)
                    RETURN count(i)
                    """
    )
    Page<Image> findByParentId(UUID parentId, Pageable pageable);

    @Query(
            value = """
                    MATCH (d:Directory { id: $parentId })
                        -[:CONTAINS*..5000]
                        ->(i:Image)
                    RETURN i
                    ORDER BY i.path ASC
                        SKIP $skip
                        LIMIT $limit
                    """,
            countQuery = """
                    MATCH (d:Directory { id: $parentId })
                        -[:CONTAINS*..5000]
                        ->(i:Image)
                    RETURN count(i)
                    """
    )
    Page<Image> findByParentIdRecursively(
            UUID parentId,
            Pageable pageable
    );
}
