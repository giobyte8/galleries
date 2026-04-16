package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.models.ScanSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface ScanScheduleRepository extends
        CrudRepository<ScanSchedule, UUID>,
        PagingAndSortingRepository<ScanSchedule, UUID> {

    @Query(
            value = """
                    MATCH (s:ScanSchedule)-[sc:SCANS]->(d:Directory)
                    WITH s, sc, d
                    ORDER BY s.createdAt DESC
                        SKIP $skip
                       LIMIT $limit
                    RETURN s, collect(sc), collect(d)
                    """,
            countQuery = """
                    MATCH (s:ScanSchedule)-[:SCANS]->(:Directory)
                    RETURN count(s)
                    """
    )
    Page<ScanSchedule> findAllWithDirectory(Pageable pageable);

    Stream<ScanSchedule> findByEnabled(boolean enabled);
}
