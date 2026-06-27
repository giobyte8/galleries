package me.giobyte8.galleries.scanner.metadata.reader.datetime;

import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.scanner.metadata.dto.GpsCoordinates;
import me.giobyte8.galleries.scanner.metadata.reader.TimeUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/// Chains multiple `DateTimeParser` implementations and tries each in
/// order based on @Order annotation. Returns the first successful result.
///
/// Ordered parsers chain:
///   1. `ZeroedDateTimeParser` — zeroed datetime (e.g., 0000:00:00 00:00:00)
///   2. `ExifDateTimeParser` — standard EXIF with optional meridiem
///   3. `LenientExifParser` — EXIF with fractional / single-digit seconds
///   4. `DottedDateParser` — dotted date separators (yyyy.MM.dd)
///   5. `IsoDateTimeParser` — ISO-8601 format
@Component
@Slf4j
public class DateTimeParsersChain {

    private final List<DateTimeParser> parsers;

    DateTimeParsersChain(List<DateTimeParser> parsers) {
        this.parsers = parsers;
    }

    /// Parses given raw datetime string using the internal chain of parsers.
    /// It uses the first successful result from trying each parser in order.
    ///
    /// Timezone resolution strategy:
    /// 1. If the raw value contains timezone information, it will be used.
    /// 2. If `tzOffset` param is provided, it will be used.
    /// 3. If `coordinates` param is provided, it will be used.
    /// 4. If none of the above, it will default to UTC.
    ///
    /// @param tzOffset Optional timezone offset string (e.g., "+02:00") to use
    ///                 if the raw value lacks timezone info.
    /// @param coordinates Optional GPS coordinates that will be used to determine
    ///                    a timezone if previous strategies fail.
    public Optional<ZonedDateTime> parse(
            String rawDateTime,
            @Nullable String tzOffset,
            @Nullable GpsCoordinates coordinates
    ) {
        ZonedDateTime dateTime = null;
        for (var parser : parsers) {
            var result = parser.parse(rawDateTime);
            if (result.isPresent()) {
                dateTime = result.get();
                break;
            }
        }

        if (dateTime == null) {
            log.warn("All datetime parsers failed for: {}", rawDateTime);
            return Optional.empty();
        }


        // Resolve timezone:

        // If raw value already provided tz info
        if (TimeUtils.containsTz(rawDateTime)) {
            return Optional.of(dateTime);
        }

        // If not, leverage tzOffset from dedicated value
        else if (StringUtils.hasText(tzOffset)) {
            var zoneOffset = ZoneOffset.of(tzOffset);
            dateTime = dateTime.withZoneSameLocal(zoneOffset);
        }

        // If not, infer timezone from coordinates if available
        else if (coordinates != null) {
            var coordsTz = TimeUtils.timezoneFor(coordinates);

            if (coordsTz.isPresent()) {
                dateTime = dateTime
                        .withZoneSameInstant(coordsTz.get());
            }
        }

        return Optional.of(dateTime);
    }

    /// Use it for testing without depending on Spring context
    public static DateTimeParsersChain defaultChain() {
        return new DateTimeParsersChain(List.of(
                new ZeroedDateTimeParser(),
                new ExifDateTimeParser(),
                new LenientExifDateTimeParser(),
                new DottedDateParser(),
                new IsoDateTimeParser()
        ));
    }
}
