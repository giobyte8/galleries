package me.giobyte8.galleries.scanner.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "dir_contains_mfile")
@IdClass(DirHasMFileId.class)
public class ContentDirHasMFile {

    public ContentDirHasMFile() {
    }

    public ContentDirHasMFile(String dirHashedPath, String fileHashedPath) {
        this.dirHashedPath = dirHashedPath;
        this.fileHashedPath = fileHashedPath;
    }

    @Id
    @Size(min = 64, max = 64)
    @Column(name = "dir_hashed_path")
    private String dirHashedPath;

    @Id
    @Size(min = 64, max = 64)
    @Column(name = "file_hashed_path")
    private String fileHashedPath;

    public String getDirHashedPath() {
        return dirHashedPath;
    }

    public void setDirHashedPath(String dirHashedPath) {
        this.dirHashedPath = dirHashedPath;
    }

    public String getFileHashedPath() {
        return fileHashedPath;
    }

    public void setFileHashedPath(String fileHashedPath) {
        this.fileHashedPath = fileHashedPath;
    }
}
