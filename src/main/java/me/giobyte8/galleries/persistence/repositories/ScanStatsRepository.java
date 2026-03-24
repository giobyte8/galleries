package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.models.ScanStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScanStatsRepository extends
        CrudRepository<ScanStats, UUID>,
        PagingAndSortingRepository<ScanStats, UUID> {

    /**
     * Find scan stats by scan request ID.
     *
     * @param scanRequestId The scan request ID
     * @return Optional containing the scan stats if found
     */
    Optional<ScanStats> findByScanRequestId(UUID scanRequestId);

    /**
     * Find all scan stats with pagination and sorting.
     * Sort is driven by the Pageable argument.
     *
     * @param pageable Pagination and sort parameters
     * @return Page of scan stats
     */
    Page<ScanStats> findAll(Pageable pageable);

    /**
     * Find scan stats filtered by directory path.
     * Sort is driven by the Pageable argument.
     *
     * @param path     The directory path to filter by
     * @param pageable Pagination and sort parameters
     * @return Page of scan stats for the given path
     */
    Page<ScanStats> findByPath(String path, Pageable pageable);
}

