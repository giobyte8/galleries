package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.models.ScanStats;
import me.giobyte8.galleries.persistence.models.ScanStatus;
import me.giobyte8.galleries.scanner.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ScanStatsRepositoryTests extends BaseIntegrationTest {

    @Autowired
    private ScanStatsRepository scanStatsRepository;

    @Test
    void saveAndFindByScanRequestId() {
        UUID scanRequestId = UUID.randomUUID();
        ScanStats stats = ScanStats.builder()
                .scanRequestId(scanRequestId)
                .path("cameras/iPhone")
                .startedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .newImages(3)
                .updatedImages(1)
                .unchangedImages(0)
                .notFoundImages(0)
                .foundDirectories(0)
                .status(ScanStatus.COMPLETED)
                .build();
        scanStatsRepository.save(stats);

        var result = scanStatsRepository.findByScanRequestId(scanRequestId);

        assertTrue(result.isPresent());
        assertEquals(stats, result.get());
    }

    @Test
    void findByScanRequestIdNonExistent() {
        var result = scanStatsRepository
                .findByScanRequestId(UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    void saveMissingCompletedAt() {
        UUID scanRequestId = UUID.randomUUID();
        ScanStats stats = ScanStats.builder()
                .scanRequestId(scanRequestId)
                .path("cameras/iPhone")
                .startedAt(LocalDateTime.now())
                .newImages(0)
                .status(ScanStatus.IN_PROGRESS)
                .build();
        scanStatsRepository.save(stats);

        var result = scanStatsRepository
                .findByScanRequestId(scanRequestId)
                .orElseThrow();

        assertNull(result.getCompletedAt());
    }

    @Test
    void saveMissingStartedAt() {
        UUID scanRequestId = UUID.randomUUID();
        ScanStats stats = ScanStats.builder()
                .scanRequestId(scanRequestId)
                .path("cameras/iPhone")
                .newImages(0)
                .status(ScanStatus.IN_PROGRESS)
                .build();
        scanStatsRepository.save(stats);

        var result = scanStatsRepository
                .findByScanRequestId(scanRequestId)
                .orElseThrow();

        assertNull(result.getStartedAt());
    }

    @Test
    void findAllPaginated() {
        scanStatsRepository.save(buildStats("cameras", UUID.randomUUID()));
        scanStatsRepository.save(buildStats("cameras/iPhone", UUID.randomUUID()));
        scanStatsRepository.save(buildStats("cameras/ca", UUID.randomUUID()));

        Page<ScanStats> page = scanStatsRepository.findAll(
                PageRequest.of(0, 2, Sort.by("path").ascending())
        );

        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getNumberOfElements());
    }

    @Test
    void findByPathPaginated() {
        String targetPath = "cameras/iPhone";
        scanStatsRepository.save(buildStats(targetPath, UUID.randomUUID()));
        scanStatsRepository.save(buildStats(targetPath, UUID.randomUUID()));
        scanStatsRepository.save(buildStats("cameras/ca", UUID.randomUUID()));

        Page<ScanStats> page = scanStatsRepository.findByPath(
                targetPath,
                PageRequest.of(0, 10)
        );

        assertEquals(2, page.getTotalElements());
        page.forEach(s ->
                assertEquals(targetPath, s.getPath())
        );
    }

    @Test
    void findByPathNoResults() {
        scanStatsRepository.save(buildStats("cameras/iPhone", UUID.randomUUID()));

        Page<ScanStats> page = scanStatsRepository.findByPath(
                "cameras/nonexistent",
                PageRequest.of(0, 10)
        );

        assertEquals(0, page.getTotalElements());
        assertTrue(page.getContent().isEmpty());
    }

    private ScanStats buildStats(String path, UUID scanRequestId) {
        return ScanStats.builder()
                .scanRequestId(scanRequestId)
                .path(path)
                .startedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .newImages(3)
                .updatedImages(0)
                .unchangedImages(0)
                .notFoundImages(0)
                .foundDirectories(0)
                .status(ScanStatus.COMPLETED)
                .build();
    }
}

