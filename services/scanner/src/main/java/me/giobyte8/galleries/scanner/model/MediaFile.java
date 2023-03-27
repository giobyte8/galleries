package me.giobyte8.galleries.scanner.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import me.giobyte8.galleries.scanner.dto.MFMetadata;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "media_file")
public class MediaFile {

    /** SHA256 Hashed version of this file path */
    @Id
    @Size(min = 64, max = 64)
    private String hashedPath;

    @NotBlank
    @Size(max = 5000)
    private String path;

    /** File content's hash */
    @NotBlank
    @Size(min = 64, max = 64)
    @Column(name = "`hash`")
    private String hash;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "`status`")
    private MediaFileStatus status = MediaFileStatus.READY;

    private Date datetimeOriginal;

    private BigDecimal gpsLatitude;

    private BigDecimal gpsLongitude;

    @Size(max = 255)
    private String camera;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        return ((MediaFile) obj).getHashedPath().equals(this.hashedPath);
    }

    @Override
    public int hashCode() {
        if (this.hashedPath == null) return super.hashCode();

        return Objects.hashCode(this.hashedPath);
    }

    /**
     * Updates this media file metadata
     * @param meta New file's metadata
     */
    public void setMetadata(MFMetadata meta) {
        this.setDatetimeOriginal(meta.getDatetimeOriginal());
        this.setGpsLatitude(meta.getGpsLatitude());
        this.setGpsLongitude(meta.getGpsLongitude());
        // TODO Set camera model
    }


    public void setHashedPath(String hashedPath) {
        this.hashedPath = hashedPath;
    }

    public String getHashedPath() {
        return hashedPath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public MediaFileStatus getStatus() {
        return status;
    }

    public void setStatus(MediaFileStatus status) {
        this.status = status;
    }

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

    public String getCamera() {
        return camera;
    }

    public void setCamera(String camera) {
        this.camera = camera;
    }
}
