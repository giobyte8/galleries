package me.giobyte8.galleries.scanner.metadata.reader.datetime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.Optional;

/// Lenient EXIF datetime parser for non-standard variants.
///
/// Handles colon-separated EXIF strings with fractional seconds (any digit
/// length) and single-digit seconds values.
///
/// Examples:
/// ```
/// 2018:06:10 11:30:33.000000
/// 2017:03:03 13:06:20.47
/// 2015:02:25 13:44:2.000000
/// ```
@Component
@Order(3)
@Slf4j
class LenientExifDateTimeParser implements DateTimeParser {

    private static final DateTimeFormatter FORMATTER =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy:MM:dd HH:mm")
                    .appendLiteral(":")
                    .appendValue(
                            ChronoField.SECOND_OF_MINUTE,
                            1, 2, SignStyle.NOT_NEGATIVE
                    )
                    .optionalStart()
                    .appendFraction(
                            ChronoField.NANO_OF_SECOND, 1, 9, true
                    )
                    .optionalEnd()
                    .toFormatter();

    @Override
    public Optional<ZonedDateTime> parse(String raw) {
        try {
            LocalDateTime localDt = LocalDateTime.parse(raw, FORMATTER);
            return Optional.of(localDt.atZone(ZoneOffset.UTC));
        } catch (DateTimeParseException e) {
            log.debug("Unable to parse as 'Lenient EXIF Datetime': {}", e.getMessage());
            return Optional.empty();
        }
    }
}
