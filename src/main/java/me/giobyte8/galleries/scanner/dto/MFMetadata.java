package me.giobyte8.galleries.scanner.dto;

import java.math.BigDecimal;
import java.util.Date;

public class MFMetadata {

    private Date datetimeOriginal;
    private BigDecimal gpsLatitude;
    private BigDecimal gpsLongitude;

    private String camMaker;

    private String camModel;

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

    public String getCamMaker() {
        return camMaker;
    }

    public void setCamMaker(String camMaker) {
        this.camMaker = camMaker;
    }

    public String getCamModel() {
        return camModel;
    }

    public void setCamModel(String camModel) {
        this.camModel = camModel;
    }
}
