package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.models.ScanStats;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScanStatsRepository extends CrudRepository<ScanStats, UUID> {

    /**
     * Find scan stats by scan request ID.
     *
     * @param scanRequestId The scan request ID
     * @return Optional containing the scan stats if found
     */
    Optional<ScanStats> findByScanRequestId(UUID scanRequestId);
}

