package me.giobyte8.galleries.schedule.services;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.admin.dto.CreateScanScheduleRequest;
import me.giobyte8.galleries.admin.dto.UpdateScanScheduleRequest;
import me.giobyte8.galleries.exceptions.DirectoryNotFoundException;
import me.giobyte8.galleries.schedule.exceptions.ScanScheduleNotFoundException;
import me.giobyte8.galleries.persistence.models.ScanSchedule;
import me.giobyte8.galleries.persistence.repositories.DirectoryRepository;
import me.giobyte8.galleries.persistence.repositories.ScanScheduleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.UUID;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class ScanSchedulesService {

    private static final Pattern TZ_OFFSET_PATTERN = Pattern.compile(
            "^[+-](0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$"
    );

    private final ScanScheduleRepository scanScheduleRepository;
    private final DirectoryRepository directoryRepository;

    @Transactional(readOnly = true)
    public Page<ScanSchedule> findAll(Pageable pageable) {
        return scanScheduleRepository.findAllWithDirectory(pageable);
    }

    @Transactional
    public ScanSchedule create(CreateScanScheduleRequest request) {
        validateSchedule(request.schedule());
        validateTzOffset(request.tzOffset());

        var directory = directoryRepository
                .findById(request.directoryId())
                .orElseThrow(() ->
                        new DirectoryNotFoundException(
                                String.valueOf(request.directoryId())
                        )
                );

        var scanSchedule = ScanSchedule.builder()
                .schedule(request.schedule())
                .tzOffset(request.tzOffset())
                .enabled(request.enabled())
                .directory(directory)
                .build();

        return scanScheduleRepository.save(scanSchedule);
    }

    @Transactional
    public ScanSchedule update(UUID id, UpdateScanScheduleRequest request) {
        validateSchedule(request.schedule());
        validateTzOffset(request.tzOffset());

        var schedule = scanScheduleRepository
                .findById(id)
                .orElseThrow(() -> new ScanScheduleNotFoundException(id));

        schedule.setSchedule(request.schedule());
        schedule.setTzOffset(request.tzOffset());
        schedule.setEnabled(request.enabled());

        return scanScheduleRepository.save(schedule);
    }

    private static void validateSchedule(String schedule) {
        try {
            CronExpression.parse(schedule);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid cron expression: " + schedule,
                    ex
            );
        }
    }

    private static void validateTzOffset(String tzOffset) {
        if (!TZ_OFFSET_PATTERN.matcher(tzOffset).matches()) {
            throw new IllegalArgumentException(
                    "tzOffset must follow +-HH:MM format"
            );
        }

        ZoneOffset.of(tzOffset);
    }
}
