package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.projections.DirWithLineage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DirectoryRepository extends
        CrudRepository<Directory, UUID>,
        PagingAndSortingRepository<Directory, UUID>,
        CustomizedDirectoryRepository {

    Optional<Directory> findByPath(String path);

    @Query(value = """
        MATCH (dir:Directory { id: $id })
        OPTIONAL MATCH path = (dir)<-[:CONTAINS*..5]-(ancestors:Directory)
        WITH dir, ancestors, path
        RETURN dir AS directory, collect(DISTINCT ancestors) AS lineage;
        """
    )
    Optional<DirWithLineage> findWithLineageById(UUID id);

    @Query(
            value = """
                    MATCH (parent:Directory { id: $parentId })
                        -[:CONTAINS]
                        ->(dir:Directory)
                    RETURN dir
                    ORDER BY dir.path ASC
                        SKIP $skip
                       LIMIT $limit""",
            countQuery = """
                    MATCH (parent:Directory { id: $parentId })
                        -[:CONTAINS]
                        ->(dir:Directory)
                    RETURN count(dir)"""
    )
    Page<Directory> findChildren(UUID parentId, Pageable pageable);

    /**
     * Finds root directories, i.e., directories that don't
     * have a parent directory.
     *
     * @return Slice of root directories sorted by path
     */
    @Query(
            value = """
                    MATCH (d:Directory)
                    WHERE NOT ( ()-[:CONTAINS]->(d) )
                    RETURN d
                    ORDER BY d.path ASC
                        SKIP $skip
                       LIMIT $limit""",
            countQuery = """
                    MATCH (d:Directory)
                    WHERE NOT ( ()-[:CONTAINS]->(d) )
                    RETURN count(d)"""
    )
    Page<Directory> findRoots(Pageable pageable);

    @Query("""
            MATCH (d:Directory)
            WHERE NOT ( ()-[:CONTAINS]->(d) )
              AND toLower(d.path) CONTAINS toLower($query)
            RETURN d
            ORDER BY d.path ASC
            LIMIT $limit
            """)
    List<Directory> searchByPath(String query, int limit);
}
