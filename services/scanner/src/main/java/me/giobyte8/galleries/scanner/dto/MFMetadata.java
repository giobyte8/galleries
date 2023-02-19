package me.giobyte8.galleries.scanner.dto;

import java.math.BigDecimal;
import java.util.Date;

public class MFMetadata {

    private Date datetimeOriginal;
    private BigDecimal gpsLatitude;
    private BigDecimal gpsLongitude;

    public Date getDatetimeOriginal() {
        return datetimeOriginal;
    }

    public void setDatetimeOriginal(Date datetimeOriginal) {
        this.datetimeOriginal = datetimeOriginal;
    }

    public BigDecimal getGpsLatitude() {
        return gpsLatitude;
    }

    public void setGpsLatitude(BigDecimal gpsLatitude) {
        this.gpsLatitude = gpsLatitude;
    }

    public BigDecimal getGpsLongitude() {
        return gpsLongitude;
    }

    public void setGpsLongitude(BigDecimal gpsLongitude) {
        this.gpsLongitude = gpsLongitude;
    }
}
