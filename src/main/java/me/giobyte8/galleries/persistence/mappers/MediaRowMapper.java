package me.giobyte8.galleries.persistence.mappers;

import me.giobyte8.galleries.dto.MediaItemDto;
import me.giobyte8.galleries.persistence.models.MediaFileStatus;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class MediaRowMapper {

    public MediaItemDto from(Record r) {
        return new MediaItemDto(
                nullable(r, "path", Value::asString),
                nullable(r, "version", Value::asLong),
                nullable(r, "fileSize", Value::asLong),
                nullable(r, "captureDateTime", Value::asZonedDateTime),
                nullable(r, "captureInstant",
                        v -> v.asOffsetDateTime().toInstant()),
                nullable(r, "rawCaptureDateTime", Value::asString),
                nullable(r, "lastModified",
                        v -> v.asOffsetDateTime().toInstant()),
                nullable(r, "gpsLatitude", Value::asDouble),
                nullable(r, "gpsLongitude", Value::asDouble),
                nullable(r, "cameraMaker", Value::asString),
                nullable(r, "cameraModel", Value::asString),
                nullable(r, "status",
                        v -> MediaFileStatus.valueOf(v.asString())),
                nullable(r, "mediaType", Value::asString)
        );
    }

    private <T> T nullable(
            Record r,
            String field,
            Function<Value, T> extractor
    ) {
        Value v = r.get(field);
        return v.isNull() ? null : extractor.apply(v);
    }
}

