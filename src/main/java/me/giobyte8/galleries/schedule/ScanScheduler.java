package me.giobyte8.galleries.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.persistence.models.ScanSchedule;
import me.giobyte8.galleries.persistence.repositories.ScanScheduleRepository;
import me.giobyte8.galleries.scanner.config.properties.ScannerProps;
import me.giobyte8.galleries.scanner.dto.ScanRequest;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class ScanScheduler {

    private final JobScheduler jobScheduler;
    private final ScanScheduleRepository scanScheduleRepo;
    private final AmqpTemplate amqpTemplate;
    private final ScannerProps scannerProps;

    /**
     * Loads all enabled scan schedules from storage and registers
     * each one as a recurring job with JobRunr.
     */
    @Deprecated
    public void scheduleRecurrentScans() {
        scanScheduleRepo
                .findByEnabled(true)
                .forEach(schedule -> {
                    log.debug(
                            "Scheduling recurrent scan: {}",
                            schedule.getId()
                    );
                    schedule(schedule);
                });
    }

    /**
     * If {@link ScanSchedule#isEnabled()} is true, registers the given
     * schedule as a recurring job in JobRunr, replacing any existing
     * job with the same ID.
     * <p>
     * If false, ensures any existing job with the same ID is unregistered.
     * <p>
     * This method should be invoked after creating/updating a scan schedule
     * to synchronize the JobRunr jobs with the current state of the schedule.
     *
     * @param schedule Scan schedule to upsert
     */
    public void upsertSchedule(ScanSchedule schedule) {
        if (schedule.isEnabled()) {
            schedule(schedule);
        } else {
            unschedule(schedule.getId());
        }
    }

    /**
     * Registers or updates a single scan schedule as a recurring
     * job in JobRunr. If a job with the same ID already exists
     * it is replaced with the new schedule and timezone.
     *
     * @param schedule The scan schedule to register.
     */
    private void schedule(ScanSchedule schedule) {
        ZoneId tzId = ZoneOffset.of(schedule.getTzOffset());

        jobScheduler.scheduleRecurrently(
                schedule.getId().toString(),
                schedule.getSchedule(),
                tzId,
                () -> requestScan(schedule.getDirectory().getPath())
        );

        log.debug(
                "Registered recurring scan job: {}",
                schedule.getId()
        );
    }

    /**
     * Removes the recurring scan job associated with the given
     * schedule ID from JobRunr.
     *
     * @param scheduleId The ID of the scan schedule to unregister.
     */
    private void unschedule(UUID scheduleId) {
        jobScheduler.deleteRecurringJob(scheduleId.toString());
        log.debug(
                "Unregistered recurring scan job: {}",
                scheduleId
        );
    }

    /**
     * Triggers scan for a directory. <br/>
     * Designed to be invoked as a Jobrunr job.
     * <p>
     * Publishes a 'scan request' message to RabbitMQ for the given
     * directory path.
     *
     * @param directoryPath Relative path of directory to scan.
     */
    public void requestScan(String directoryPath) {
        var scanRequest = new ScanRequest(
                UUID.randomUUID(),
                directoryPath,
                LocalDateTime.now()
        );

        try {
            amqpTemplate.convertAndSend(
                    scannerProps.getAmqp().getQueueScanRequests(),
                    scanRequest
            );
        } catch (AmqpException e) {
            log.error(
                    "Failed to publish scheduled scan request for: {}",
                    directoryPath,
                    e
            );

            // Re-throw so JobRunr marks the job as failed and
            // triggers its built-in retry mechanism.
            throw e;
        }
    }
}
