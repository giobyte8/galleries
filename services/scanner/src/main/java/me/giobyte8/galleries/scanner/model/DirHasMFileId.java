package me.giobyte8.galleries.scanner.model;

import java.io.Serializable;
import java.util.Objects;

public class DirHasMFileId implements Serializable {

    private String dirHashedPath;
    private String fileHashedPath;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        DirHasMFileId hasMFileId = (DirHasMFileId) obj;
        return Objects.equals(dirHashedPath, hasMFileId.dirHashedPath) &&
                Objects.equals(fileHashedPath, hasMFileId.fileHashedPath);
    }

    @Override
    public int hashCode() {
        if (this.dirHashedPath == null && this.fileHashedPath == null) {
            return super.hashCode();
        }

        return Objects.hash(this.dirHashedPath, this.fileHashedPath);
    }

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
