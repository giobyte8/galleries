package me.giobyte8.galleries.scanner.metadata.reader.datetime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/// Parses ISO-8601 datetime strings.
///
/// Handles `yyyy-MM-dd'T'HH:mm:ss` with optional timezone offset
/// and optional fractional seconds.
///
/// Examples:
/// ```
/// 2023-03-31T15:31:27+01:00
/// 2014-03-16T10:57:25
/// ```
@Component
@Order(5)
@Slf4j
class IsoDateTimeParser implements DateTimeParser {

    @Override
    public Optional<ZonedDateTime> parse(String raw) {
        try {
            return Optional.of(ZonedDateTime.parse(raw));
        } catch (DateTimeParseException e) {

            try {
                LocalDateTime localDt = LocalDateTime.parse(
                        raw, DateTimeFormatter.ISO_LOCAL_DATE_TIME
                );
                return Optional.of(localDt.atZone(ZoneOffset.UTC));

            } catch (DateTimeParseException e2) {
                log.debug("Unable to parse as 'ISO DateTime': {}", e2.getMessage());
                return Optional.empty();
            }
        }
    }
}
