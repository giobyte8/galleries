package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.models.Directory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DirectoryRepository extends
        CrudRepository<Directory, String>,
        PagingAndSortingRepository<Directory, String>,
        CustomizedDirectoryRepository {

    Directory findByPath(String path);

    @Query("""
            MATCH (parent:Directory { path: $parentPath })
                -[:CONTAINS]
                ->(dir:Directory)
            RETURN dir
            ORDER BY dir.path ASC;""")
    List<Directory> findChildren(String parentPath);

    @Query(
            value = """
                    MATCH (parent:Directory { path: $parentPath })
                        -[:CONTAINS]
                        ->(dir:Directory)
                    RETURN dir
                    ORDER BY dir.path ASC
                        SKIP $skip
                       LIMIT $limit""",
            countQuery = """
                    MATCH (parent:Directory { path: $parentPath })
                        -[:CONTAINS]
                        ->(dir:Directory)
                    RETURN count(dir)"""
    )
    Page<Directory> findChildren(String parentPath, Pageable pageable);

    /**
     * Finds root directories, i.e., directories that don't
     * have a parent directory.
     *
     * @return List of root directories sorted by path
     */
    @Query("""
            MATCH (d:Directory)
            WHERE NOT ( ()-[:CONTAINS]->(d) )
            RETURN d
            ORDER BY d.path ASC;""")
    List<Directory> findRoots();

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
}
