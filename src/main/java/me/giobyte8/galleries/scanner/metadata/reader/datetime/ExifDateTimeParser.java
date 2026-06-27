package me.giobyte8.galleries.scanner.metadata.reader.datetime;

import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.scanner.metadata.reader.TimeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Optional;

/// Parses standard EXIF datetime strings.
///
/// Handles `yyyy:MM:dd HH:mm:ss` with optional `a.m.`/`p.m.`
/// meridiem markers and optional timezone offset (`+HH:MM` or `+HHMM`).
///
/// Examples:
/// ```
/// 2025:11:08 12:15:12p.m.+07:00
/// 2025:11:08 12:15:12p.m.
/// 2024:09:27 02:17+05:30
/// ```
@Component
@Order(2)
@Slf4j
class ExifDateTimeParser implements DateTimeParser {

    private static final DateTimeFormatter EXIF_FORMATTER =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy:MM:dd HH:mm")
                    .optionalStart()
                    .appendLiteral(":")
                    .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                    .optionalEnd()
                    .optionalStart()
                    .appendLiteral("a.m.")
                    .optionalEnd()
                    .optionalStart()
                    .appendLiteral("p.m.")
                    .optionalEnd()
                    .toFormatter();

    private static final DateTimeFormatter EXIF_TZ_FORMATTER =
            new DateTimeFormatterBuilder()
                    .append(EXIF_FORMATTER)
                    .optionalStart()
                    .appendOffset("+HH:MM", "+00:00")
                    .optionalEnd()
                    .optionalStart()
                    .appendOffset("+HHMM", "+0000")
                    .optionalEnd()
                    .toFormatter();

    @Override
    public Optional<ZonedDateTime> parse(String raw) {
        try {
            if (TimeUtils.containsTz(raw)) {
                return Optional.of(
                        OffsetDateTime
                                .parse(raw, EXIF_TZ_FORMATTER)
                                .toZonedDateTime()
                );
            }

            return Optional.of(
                    LocalDateTime
                            .parse(raw, EXIF_FORMATTER)

                            // mp4/Samsung videos stores datetime in UTC by
                            // ISO standard; hence, we assume it is in UTC
                            .atZone(ZoneOffset.UTC)
            );
        } catch (DateTimeParseException e) {
            log.debug(
                    "Unable to parse as 'EXIF Datetime': {}",
                    e.getMessage()
            );

            return Optional.empty();
        }
    }
}
