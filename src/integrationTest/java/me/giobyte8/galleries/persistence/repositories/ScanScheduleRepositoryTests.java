package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.ScanSchedule;
import me.giobyte8.galleries.scanner.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScanScheduleRepositoryTests extends BaseIntegrationTest {

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private ScanScheduleRepository scanScheduleRepository;

    @Test
    void saveAndFindById() {
        var directory = directoryRepository.save(
                Directory.builder().path("root/holidays").build()
        );

        var schedule = scanScheduleRepository.save(
                ScanSchedule.builder()
                        .schedule("0 */30 * * * *")
                        .tzOffset("+02:00")
                        .enabled(true)
                        .directory(directory)
                        .build()
        );

        var foundOpt = scanScheduleRepository.findById(schedule.getId());
        assertTrue(foundOpt.isPresent());

        var found = foundOpt.get();
        assertEquals("0 */30 * * * *", found.getSchedule());
        assertEquals("+02:00", found.getTzOffset());
        assertTrue(found.isEnabled());
        assertEquals("root/holidays", found.getDirectory().getPath());
        assertNotNull(found.getCreatedAt());
        assertNotNull(found.getUpdatedAt());
    }

    @Test
    void findAllPaginated() {
        var directory = directoryRepository.save(
                Directory.builder().path("root/weddings").build()
        );

        scanScheduleRepository.save(
                ScanSchedule.builder()
                        .schedule("0 */20 * * * *")
                        .tzOffset("+01:00")
                        .enabled(true)
                        .directory(directory)
                        .build()
        );

        scanScheduleRepository.save(
                ScanSchedule.builder()
                        .schedule("0 0 * * * *")
                        .tzOffset("+01:00")
                        .enabled(true)
                        .directory(directory)
                        .build()
        );

        var page = scanScheduleRepository.findAll(
                PageRequest.of(0, 1)
        );

        assertEquals(2, page.getTotalElements());
        assertEquals(1, page.getNumberOfElements());
        assertNotNull(page.getContent().getFirst().getSchedule());
    }
}


