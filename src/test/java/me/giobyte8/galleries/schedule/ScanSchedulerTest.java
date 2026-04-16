package me.giobyte8.galleries.schedule;

import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.ScanSchedule;
import me.giobyte8.galleries.persistence.repositories.ScanScheduleRepository;
import me.giobyte8.galleries.scanner.config.properties.ScannerProps;
import me.giobyte8.galleries.scanner.dto.ScanRequest;
import org.jobrunr.jobs.lambdas.JobLambda;
import org.jobrunr.scheduling.JobScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScanSchedulerTest {

    @Mock
    private JobScheduler jobScheduler;

    @Mock
    private ScanScheduleRepository scanScheduleRepo;

    @Mock
    private AmqpTemplate amqpTemplate;

    @Mock
    private ScannerProps scannerProps;

    @Mock
    private ScannerProps.AMQPProps amqpProps;

    @InjectMocks
    private ScanScheduler scanScheduler;

    @BeforeEach
    void setUp() {
        // Used only by requestScan tests; lenient to avoid strict-stubbing
        // failures in tests that don't invoke requestScan.
        lenient().when(scannerProps.getAmqp()).thenReturn(amqpProps);
        lenient().when(amqpProps.getQueueScanRequests())
                .thenReturn("GL_SCAN_REQUESTS");
    }

    // ---------------------------------------------------------------
    // scheduleRecurrentScans
    // ---------------------------------------------------------------

    @Test
    void scheduleRecurrentScans_registersAllEnabledSchedules() {
        var dir1 = directoryWithPath("cameras");
        var dir2 = directoryWithPath("videos");

        var schedule1 = enabledSchedule(dir1);
        var schedule2 = enabledSchedule(dir2);

        when(scanScheduleRepo.findByEnabled(true))
                .thenReturn(Stream.of(schedule1, schedule2));

        scanScheduler.scheduleRecurrentScans();

        verify(jobScheduler, times(2))
                .scheduleRecurrently(
                        anyString(),
                        anyString(),
                        any(),
                        any(JobLambda.class)
                );
    }

    @Test
    void scheduleRecurrentScans_doesNothing_whenNoEnabledSchedules() {
        when(scanScheduleRepo.findByEnabled(true))
                .thenReturn(Stream.empty());

        scanScheduler.scheduleRecurrentScans();

        verify(jobScheduler, never())
                .scheduleRecurrently(
                        anyString(),
                        anyString(),
                        any(),
                        any(JobLambda.class)
                );
    }

    // ---------------------------------------------------------------
    // registerSchedule
    // ---------------------------------------------------------------

    @Test
    void upsertSchedule_callsScheduleRecurrently_forEnabledSchedule() {
        var dir = directoryWithPath("cameras/iPhone");
        var schedule = enabledSchedule(dir);

        scanScheduler.upsertSchedule(schedule);

        verify(jobScheduler).scheduleRecurrently(
                eq(schedule.getId().toString()),
                eq(schedule.getSchedule()),
                any(),
                any(JobLambda.class)
        );
    }

    @Test
    void upsertSchedule_callsDelete_whenScheduleIsDisabled() {
        var dir = directoryWithPath("cameras");
        var schedule = enabledSchedule(dir);
        schedule.setEnabled(false);

        scanScheduler.upsertSchedule(schedule);

        verify(jobScheduler, never())
                .scheduleRecurrently(
                        anyString(),
                        anyString(),
                        any(),
                        any(JobLambda.class)
                );
        verify(jobScheduler).deleteRecurringJob(schedule.getId().toString());
    }

    // ---------------------------------------------------------------
    // requestScan
    // ---------------------------------------------------------------

    @Test
    void requestScan_publishesScanRequestToAmqp() {
        scanScheduler.requestScan("cameras/iPhone");

        ArgumentCaptor<Object> messageCaptor =
                ArgumentCaptor.forClass(Object.class);
        verify(amqpTemplate).convertAndSend(
                eq("GL_SCAN_REQUESTS"),
                messageCaptor.capture()
        );

        Object message = messageCaptor.getValue();
        assertThat(message).isInstanceOf(ScanRequest.class);
        ScanRequest scanRequest = (ScanRequest) message;
        assertThat(scanRequest.path()).isEqualTo("cameras/iPhone");
        assertThat(scanRequest.id()).isNotNull();
        assertThat(scanRequest.requestedAt()).isNotNull();
    }

    @Test
    void requestScan_rethrowsAmqpException() {
        doThrow(new AmqpException("connection refused"))
                .when(amqpTemplate)
                .convertAndSend(anyString(), any(Object.class));

        assertThatThrownBy(() ->
                scanScheduler.requestScan("cameras")
        ).isInstanceOf(AmqpException.class);
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private Directory directoryWithPath(String path) {
        return Directory.builder()
                .id(UUID.randomUUID())
                .path(path)
                .build();
    }

    private ScanSchedule enabledSchedule(Directory directory) {
        return ScanSchedule.builder()
                .id(UUID.randomUUID())
                .schedule("0 */15 * * * *")
                .tzOffset("+00:00")
                .enabled(true)
                .directory(directory)
                .build();
    }
}





