package me.giobyte8.galleries.scanner.metadata.reader.datetime;

import java.time.ZonedDateTime;
import java.util.Optional;

/// Attempts to parse a raw datetime string into a `ZonedDateTime`.
/// Each implementation targets a specific format family. Returns empty when
/// the string does not match its expected pattern.
@FunctionalInterface
interface DateTimeParser {
    Optional<ZonedDateTime> parse(String raw);
}
