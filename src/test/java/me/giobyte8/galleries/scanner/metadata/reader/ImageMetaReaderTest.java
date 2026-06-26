package me.giobyte8.galleries.scanner.metadata.reader;

import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import me.giobyte8.galleries.models.MediaFormat;
import me.giobyte8.galleries.scanner.metadata.dto.MediaDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static com.drew.metadata.exif.ExifDirectoryBase.TAG_DATETIME_ORIGINAL;
import static com.drew.metadata.exif.ExifDirectoryBase.TAG_TIME_ZONE_ORIGINAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageMetaReaderTest {

    @Test
    void captureDateTimeSupportsMeridiemWithEmbeddedTimezone() {
        var rawCaptureDate = "2025:11:08 12:15:12p.m.+07:00";

        var captureDate = assertCaptureDateTime(rawCaptureDate, null, false);

        assertEquals(2025, captureDate.getYear());
        assertEquals(11, captureDate.getMonthValue());
        assertEquals(8, captureDate.getDayOfMonth());
        assertEquals(12, captureDate.getHour());
        assertEquals(15, captureDate.getMinute());
        assertEquals(12, captureDate.getSecond());
        assertEquals(ZoneOffset.ofHours(7), captureDate.getOffset());
    }

    @Test
    void captureDateTimeSupportsMeridiemWithoutTimezone() {
        var rawCaptureDate = "2025:11:08 12:15:12p.m.";

        var captureDate = assertCaptureDateTime(rawCaptureDate, null, true);

        assertEquals(2025, captureDate.getYear());
        assertEquals(11, captureDate.getMonthValue());
        assertEquals(8, captureDate.getDayOfMonth());
        assertEquals(12, captureDate.getHour());
        assertEquals(15, captureDate.getMinute());
        assertEquals(12, captureDate.getSecond());
        assertEquals(ZoneOffset.UTC, captureDate.getOffset());
    }

    @Test
    void captureDateTimeSupports24HourValuesWithMeridiemAndTimezone() {
        var rawCaptureDate = "2025:11:14 15:53:36p.m.+07:00";

        var captureDate = assertCaptureDateTime(rawCaptureDate, null, false);

        assertEquals(2025, captureDate.getYear());
        assertEquals(11, captureDate.getMonthValue());
        assertEquals(14, captureDate.getDayOfMonth());
        assertEquals(15, captureDate.getHour());
        assertEquals(53, captureDate.getMinute());
        assertEquals(36, captureDate.getSecond());
        assertEquals(ZoneOffset.ofHours(7), captureDate.getOffset());
    }

    @Test
    void captureDateTimeSupports24HourValuesWithMeridiemWithoutTimezone() {
        var rawCaptureDate = "2025:11:14 15:53:36p.m.";

        var captureDate = assertCaptureDateTime(rawCaptureDate, null, true);

        assertEquals(2025, captureDate.getYear());
        assertEquals(11, captureDate.getMonthValue());
        assertEquals(14, captureDate.getDayOfMonth());
        assertEquals(15, captureDate.getHour());
        assertEquals(53, captureDate.getMinute());
        assertEquals(36, captureDate.getSecond());
        assertEquals(ZoneOffset.UTC, captureDate.getOffset());
    }

    @Test
    void captureDateTimeSupportsMeridiemWithSeparateTimezoneTag() {
        var rawCaptureDate = "2025:11:08 12:15:12p.m.";

        var captureDate = assertCaptureDateTime(rawCaptureDate, "+07:00", false);

        assertEquals(2025, captureDate.getYear());
        assertEquals(11, captureDate.getMonthValue());
        assertEquals(8, captureDate.getDayOfMonth());
        assertEquals(12, captureDate.getHour());
        assertEquals(15, captureDate.getMinute());
        assertEquals(12, captureDate.getSecond());
        assertEquals(ZoneOffset.ofHours(7), captureDate.getOffset());
    }

    private ZonedDateTime assertCaptureDateTime(
            String rawCaptureDate,
            String timeZoneOriginal,
            boolean expectGpsLookup
    ) {
        var meta = mock(Metadata.class);
        var exifDir = mockDatetimeOriginal(rawCaptureDate, timeZoneOriginal);
        when(meta.getDirectoriesOfType(ExifSubIFDDirectory.class))
                .thenReturn(List.of(exifDir));
        lenient().when(meta.getDirectoriesOfType(GpsDirectory.class))
                .thenReturn(List.of());

        var datetimeOpt = new ImageMetaReader(MediaFormat.Jpeg, meta).captureDateTime();
        assertTrue(datetimeOpt.isPresent());

        MediaDateTime mediaDateTime = datetimeOpt.get();
        assertEquals(rawCaptureDate, mediaDateTime.raw());

        if (expectGpsLookup) {
            verify(meta).getDirectoriesOfType(GpsDirectory.class);
        } else {
            verify(meta, never()).getDirectoriesOfType(GpsDirectory.class);
        }

        return mediaDateTime.datetime();
    }

    private ExifSubIFDDirectory mockDatetimeOriginal(
            String rawTime,
            String timeZoneOriginal
    ) {
        var mExifDir = mock(ExifSubIFDDirectory.class);

        when(mExifDir.containsTag(TAG_DATETIME_ORIGINAL)).thenReturn(true);
        when(mExifDir.getString(TAG_DATETIME_ORIGINAL)).thenReturn(rawTime);
        when(mExifDir.getString(TAG_TIME_ZONE_ORIGINAL))
                .thenReturn(timeZoneOriginal);

        return mExifDir;
    }
}