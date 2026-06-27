package me.giobyte8.galleries.scanner.metadata.reader.datetime;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

/// Handles datetime strings that had been default to '0's.
/// Such dates are parsed as the minimum possible datetime.
///
/// Examples:
/// ```
/// 0000:00:00 00:00:00
/// 0000:00:00 00:00:00+00:00 (?)
/// ```
@Component
@Order(1)
public class ZeroedDateTimeParser implements DateTimeParser {

    @Override
    public Optional<ZonedDateTime> parse(String raw) {

        // Does it work for '0000:00:00 00:00:00+00:00'? (Offset included)
        if (raw.matches("^0{4}:0{2}:0{2} 0{2}:0{2}:0{2}$")) {
            var minDate = ZonedDateTime.of(
                    0, 1, 1,
                    0, 0, 0, 0,
                    ZoneOffset.UTC);

            return Optional.of(minDate);
        }

        return Optional.empty();
    }
}
