package me.giobyte8.galleries.scanner.metadata.dto;

import lombok.Builder;

import java.time.ZonedDateTime;

/**
 * Media files have different datetime values in its metadata:
 * capture datetime, creation datetime, modification datetime, etc.
 * Each of those values may have or not have timezone data.
 * <p>
 * For practicality, we keep the raw value as recovered from media files via
 * metadata reader libraries, and also the parsed/adjusted datetime with right
 * timezone information.
 * <p>
 * Usually these values should be the same, however, we keep both to handle
 * edge cases appropriately.
 *
 * @param raw Value as returned by metadata reading libraries (no
 *            conversion/parsing applied).
 * @param datetime Parsed raw value including timezone info or assuming UTC as
 *                 default if not timezone data was found.
 */
@Builder
public record MediaDateTime (
        String raw,
        ZonedDateTime datetime
) {
}
