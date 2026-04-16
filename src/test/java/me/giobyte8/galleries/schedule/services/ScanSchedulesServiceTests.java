package me.giobyte8.galleries.schedule.services;

import me.giobyte8.galleries.admin.dto.CreateScanScheduleRequest;
import me.giobyte8.galleries.admin.dto.UpdateScanScheduleRequest;
import me.giobyte8.galleries.exceptions.DirectoryNotFoundException;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.ScanSchedule;
import me.giobyte8.galleries.persistence.repositories.DirectoryRepository;
import me.giobyte8.galleries.persistence.repositories.ScanScheduleRepository;
import me.giobyte8.galleries.schedule.ScanScheduler;
import me.giobyte8.galleries.schedule.exceptions.ScanScheduleNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScanSchedulesServiceTests {

    @Mock
    private ScanScheduleRepository scanScheduleRepository;

    @Mock
    private DirectoryRepository directoryRepository;

    @Mock
    private ScanScheduler scanScheduler;

    @InjectMocks
    private ScanSchedulesService scanSchedulesService;

    @Test
    void createValidSchedule() {
        var directoryId = UUID.randomUUID();
        var directory = Directory.builder()
                .id(directoryId)
                .path("root/photos")
                .build();

        when(directoryRepository.findById(directoryId))
                .thenReturn(Optional.of(directory));
        when(scanScheduleRepository.save(any(ScanSchedule.class)))
                .thenAnswer(invocation -> {
                    ScanSchedule schedule = invocation.getArgument(0);
                    // Simulate Spring Data auditing
                    if (schedule.getCreatedAt() == null) {
                        schedule.setCreatedAt(LocalDateTime.now());
                    }
                    if (schedule.getUpdatedAt() == null) {
                        schedule.setUpdatedAt(LocalDateTime.now());
                    }
                    return schedule;
                });

        var request = new CreateScanScheduleRequest(
                directoryId,
                "0 */15 * * * *",
                "+01:00",
                true
        );

        var saved = scanSchedulesService.create(request);

        assertEquals("0 */15 * * * *", saved.getSchedule());
        assertEquals("+01:00", saved.getTzOffset());
        assertTrue(saved.isEnabled());
        assertEquals(directory, saved.getDirectory());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());

        ArgumentCaptor<ScanSchedule> captor =
                ArgumentCaptor.forClass(ScanSchedule.class);
        verify(scanScheduleRepository).save(captor.capture());
        assertEquals("0 */15 * * * *", captor.getValue().getSchedule());

        // Enabled schedule should be registered with the scheduler
        verify(scanScheduler).upsertSchedule(saved);
    }

    @Test
    void createInvalidCronExpression() {
        var request = new CreateScanScheduleRequest(
                UUID.randomUUID(),
                "invalid cron",
                "+01:00",
                true
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> scanSchedulesService.create(request)
        );
    }

    @Test
    void createInvalidTzOffset() {
        var request = new CreateScanScheduleRequest(
                UUID.randomUUID(),
                "0 */15 * * * *",
                "+0100",
                true
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> scanSchedulesService.create(request)
        );
    }

    @Test
    void createWithUnknownDirectory() {
        var directoryId = UUID.randomUUID();
        when(directoryRepository.findById(directoryId))
                .thenReturn(Optional.empty());

        var request = new CreateScanScheduleRequest(
                directoryId,
                "0 */15 * * * *",
                "+01:00",
                true
        );

        assertThrows(
                DirectoryNotFoundException.class,
                () -> scanSchedulesService.create(request)
        );
    }

    @Test
    void updateSchedule() {
        var scheduleId = UUID.randomUUID();
        var schedule = ScanSchedule.builder()
                .id(scheduleId)
                .schedule("0 */15 * * * *")
                .tzOffset("+01:00")
                .enabled(true)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(scanScheduleRepository.findById(scheduleId))
                .thenReturn(Optional.of(schedule));
        when(scanScheduleRepository.save(any(ScanSchedule.class)))
                .thenAnswer(invocation -> {
                    ScanSchedule s = invocation.getArgument(0);
                    // Simulate Spring Data auditing updating the timestamp
                    s.setUpdatedAt(LocalDateTime.now());
                    return s;
                });

        var request = new UpdateScanScheduleRequest(
                "0 0 * * * *",
                "-03:00",
                false
        );

        var updated = scanSchedulesService.update(scheduleId, request);

        assertEquals("0 0 * * * *", updated.getSchedule());
        assertEquals("-03:00", updated.getTzOffset());
        assertFalse(updated.isEnabled());
        assertTrue(updated.getUpdatedAt().isAfter(updated.getCreatedAt()));

        // Disabled schedule should be unregistered from the scheduler
        verify(scanScheduler).upsertSchedule(updated);
    }

    @Test
    void updateUnknownSchedule() {
        var scheduleId = UUID.randomUUID();
        when(scanScheduleRepository.findById(scheduleId))
                .thenReturn(Optional.empty());

        var request = new UpdateScanScheduleRequest(
                "0 0 * * * *",
                "+00:00",
                true
        );

        assertThrows(
                ScanScheduleNotFoundException.class,
                () -> scanSchedulesService.update(scheduleId, request)
        );
    }
}



