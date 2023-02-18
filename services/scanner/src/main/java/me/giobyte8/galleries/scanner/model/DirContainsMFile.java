package me.giobyte8.galleries.scanner.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "dir_contains_mfile")
public class DirContainsMFile {

    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid4")
    private String id;

    @NotBlank
    @Size(min = 64, max = 64)
    @Column(name = "dir_hashed_path")
    private String dirHashedPath;

    @NotBlank
    @Size(min = 64, max = 64)
    @Column(name = "file_hashed_path")
    private String fileHashedPath;


    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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
