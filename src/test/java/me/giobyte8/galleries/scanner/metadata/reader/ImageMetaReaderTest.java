package me.giobyte8.galleries.scanner.metadata.reader;

import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import me.giobyte8.galleries.models.MediaFormat;
import me.giobyte8.galleries.scanner.metadata.dto.MediaDateTime;
import me.giobyte8.galleries.scanner.metadata.reader.datetime.DateTimeParsersChain;
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

    private static final DateTimeParsersChain PARSERS =
            DateTimeParsersChain.defaultChain();

    @Test
    void captureDateTimeSupportsMeridiemWithEmbeddedTimezone() {
        var rawCaptureDate = "2025:11:08 12:15:12p.m.+07:00";

        var captureDate = assertCaptureDateTime(rawCaptureDate, null);

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

        var captureDate = assertCaptureDateTime(rawCaptureDate, null);

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

        var captureDate = assertCaptureDateTime(rawCaptureDate, null);

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

        var captureDate = assertCaptureDateTime(rawCaptureDate, null);

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

        var captureDate = assertCaptureDateTime(rawCaptureDate, "+07:00");

        assertEquals(2025, captureDate.getYear());
        assertEquals(11, captureDate.getMonthValue());
        assertEquals(8, captureDate.getDayOfMonth());
        assertEquals(12, captureDate.getHour());
        assertEquals(15, captureDate.getMinute());
        assertEquals(12, captureDate.getSecond());
        assertEquals(ZoneOffset.ofHours(7), captureDate.getOffset());
    }

    @Test
    void captureDateTimeDottedDateFormat() {
        var raw = "2020.11.22 17:09:03";
        var captureDate = assertCaptureDateTime(raw, null);

        assertEquals(2020, captureDate.getYear());
        assertEquals(11, captureDate.getMonthValue());
        assertEquals(22, captureDate.getDayOfMonth());
        assertEquals(17, captureDate.getHour());
        assertEquals(9, captureDate.getMinute());
        assertEquals(3, captureDate.getSecond());
        assertEquals(ZoneOffset.UTC, captureDate.getOffset());
    }

    @Test
    void captureDateTimeIsoFormatWithTimezone() {
        var raw = "2023-03-31T15:31:27+01:00";
        var captureDate = assertCaptureDateTime(raw, null);

        assertEquals(2023, captureDate.getYear());
        assertEquals(3, captureDate.getMonthValue());
        assertEquals(31, captureDate.getDayOfMonth());
        assertEquals(15, captureDate.getHour());
        assertEquals(31, captureDate.getMinute());
        assertEquals(27, captureDate.getSecond());
        assertEquals(ZoneOffset.ofHours(1), captureDate.getOffset());
    }

    @Test
    void captureDateTimeIsoFormatWithoutTimezone() {
        var raw = "2014-03-16T10:57:25";
        var captureDate = assertCaptureDateTime(raw, null);

        assertEquals(2014, captureDate.getYear());
        assertEquals(3, captureDate.getMonthValue());
        assertEquals(16, captureDate.getDayOfMonth());
        assertEquals(10, captureDate.getHour());
        assertEquals(57, captureDate.getMinute());
        assertEquals(25, captureDate.getSecond());
        assertEquals(ZoneOffset.UTC, captureDate.getOffset());
    }

    @Test
    void captureDateTimeExifWithFractionalSeconds() {
        var raw = "2018:06:10 11:30:33.000000";
        var captureDate = assertCaptureDateTime(raw, null);

        assertEquals(2018, captureDate.getYear());
        assertEquals(6, captureDate.getMonthValue());
        assertEquals(10, captureDate.getDayOfMonth());
        assertEquals(11, captureDate.getHour());
        assertEquals(30, captureDate.getMinute());
        assertEquals(33, captureDate.getSecond());
        assertEquals(ZoneOffset.UTC, captureDate.getOffset());
    }

    @Test
    void captureDateTimeExifWithShortFractional() {
        var raw = "2017:03:03 13:06:20.47";
        var captureDate = assertCaptureDateTime(raw, null);

        assertEquals(2017, captureDate.getYear());
        assertEquals(3, captureDate.getMonthValue());
        assertEquals(3, captureDate.getDayOfMonth());
        assertEquals(13, captureDate.getHour());
        assertEquals(6, captureDate.getMinute());
        assertEquals(20, captureDate.getSecond());
        assertEquals(ZoneOffset.UTC, captureDate.getOffset());
    }

    @Test
    void captureDateTimeExifWithSingleDigitSeconds() {
        var raw = "2015:02:25 13:44:2.000000";
        var captureDate = assertCaptureDateTime(raw, null);

        assertEquals(2015, captureDate.getYear());
        assertEquals(2, captureDate.getMonthValue());
        assertEquals(25, captureDate.getDayOfMonth());
        assertEquals(13, captureDate.getHour());
        assertEquals(44, captureDate.getMinute());
        assertEquals(2, captureDate.getSecond());
        assertEquals(ZoneOffset.UTC, captureDate.getOffset());
    }

    @Test
    void captureDateTimeReturnsEmptyForSentinel() {
        var rawCaptureDate = "0000:00:00 00:00:00";
        var captureDate = assertCaptureDateTime(rawCaptureDate, null);

        assertEquals(0, captureDate.getYear());
        assertEquals(1, captureDate.getMonthValue());
        assertEquals(1, captureDate.getDayOfMonth());
        assertEquals(0, captureDate.getHour());
        assertEquals(0, captureDate.getMinute());
        assertEquals(0, captureDate.getSecond());
        assertEquals(ZoneOffset.UTC, captureDate.getOffset());
    }

    @Test
    void captureDateTimeSupportsNoSecondsWithEmbeddedTimezone() {
        var rawCaptureDate = "2024:09:27 02:17+05:30";
        var captureDate = assertCaptureDateTime(rawCaptureDate, null);

        assertEquals(2024, captureDate.getYear());
        assertEquals(9, captureDate.getMonthValue());
        assertEquals(27, captureDate.getDayOfMonth());
        assertEquals(2, captureDate.getHour());
        assertEquals(17, captureDate.getMinute());
        assertEquals(0, captureDate.getSecond());

        assertEquals(ZoneOffset.ofHoursMinutes(5, 30), captureDate.getOffset());
    }

    private ZonedDateTime assertCaptureDateTime(
            String rawCaptureDate,
            String timeZoneOriginal
    ) {
        var meta = mock(Metadata.class);
        var exifDir = mockDatetimeOriginal(rawCaptureDate, timeZoneOriginal);
        when(meta.getDirectoriesOfType(ExifSubIFDDirectory.class))
                .thenReturn(List.of(exifDir));
        when(meta.getDirectoriesOfType(GpsDirectory.class))
                .thenReturn(List.of());

        var datetimeOpt = new ImageMetaReader(PARSERS, MediaFormat.Jpeg, meta).captureDateTime();
        assertTrue(datetimeOpt.isPresent());

        MediaDateTime mediaDateTime = datetimeOpt.get();
        assertEquals(rawCaptureDate, mediaDateTime.raw());

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