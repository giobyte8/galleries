package me.giobyte8.galleries.schedule.exceptions;

import java.util.UUID;

public class ScanScheduleNotFoundException extends RuntimeException {

    public ScanScheduleNotFoundException(UUID id) {
        super("Scan schedule not found: " + id);
    }
}

