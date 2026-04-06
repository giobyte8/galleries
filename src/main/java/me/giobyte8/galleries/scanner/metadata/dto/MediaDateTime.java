package me.giobyte8.galleries.scanner.metadata.dto;

import lombok.Builder;

import java.time.ZonedDateTime;

/**
 * Media files follow different approaches to store "capture" datetime in its
 * metadata. Value is usually stored as a plain string without a clear standard
 * format, also, timezone data is not always included in it.
 * <p>
 * For practicality, we keep the raw string value, and we also parse it into a
 * datetime with timezone information. If timezone is not specified in file
 * metadata, then we assume it is in UTC timezone.
 *
 * @param raw Value as stored in file metadata (no conversion/parsing applied).
 * @param datetime Parsed raw value including its Timezone info or using UTC as
 *                 default if not timezone data was found.
 */
@Builder
public record MediaDateTime (
        String raw,
        ZonedDateTime datetime
) {
}
