package me.giobyte8.galleries.scanner.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Date;

@Entity
@Table(name = "content_dir")
public class ContentDir {

    /** SHA256 Hashed version of directory path */
    @Id
    @Size(min = 64, max = 64)
    private String hashedPath;

    @NotBlank
    @Size(max = 5000)
    private String path;

    @NotNull
    @Column(name = "`recursive`")
    private boolean recursive = false;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "`status`")
    private ContentDirStatus status = ContentDirStatus.SCAN_PENDING;

    private Date lastScanStart;
    private Date lastScanCompletion;

    public String getHashedPath() {
        return hashedPath;
    }

    public void setHashedPath(String hashedPath) {
        this.hashedPath = hashedPath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public ContentDirStatus getStatus() {
        return status;
    }

    public void setStatus(ContentDirStatus status) {
        this.status = status;
    }

    public Date getLastScanStart() {
        return lastScanStart;
    }

    public void setLastScanStart(Date lastScanStart) {
        this.lastScanStart = lastScanStart;
    }

    public Date getLastScanCompletion() {
        return lastScanCompletion;
    }

    public void setLastScanCompletion(Date lastScanCompletion) {
        this.lastScanCompletion = lastScanCompletion;
    }

}
