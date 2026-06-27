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

/// Parses datetime strings using dots as date-part separators.
///
/// Handles `yyyy.MM.dd HH:mm:ss`.
///
/// Examples:
/// ```
/// 2020.11.22 17:09:03
/// ```
@Component
@Order(4)
@Slf4j
class DottedDateParser implements DateTimeParser {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

    @Override
    public Optional<ZonedDateTime> parse(String raw) {
        try {
            LocalDateTime localDt = LocalDateTime.parse(raw, FORMATTER);
            return Optional.of(localDt.atZone(ZoneOffset.UTC));
        } catch (DateTimeParseException e) {
            log.debug("Unable to parse as 'Dotted Date': {}", e.getMessage());
            return Optional.empty();
        }
    }
}
